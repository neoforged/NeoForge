/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.summary;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class JUnitSummaryFormatter implements FileSummaryFormatter {
    private final Path outputDir;

    public JUnitSummaryFormatter(Path outputDir) {
        this.outputDir = outputDir;
    }

    @Override
    public Path outputPath(ResourceLocation frameworkId) {
        return outputDir.resolve("testframework-" + frameworkId.toString().replace(':', '-') + "-" + Instant.now().truncatedTo(ChronoUnit.SECONDS).toString().replaceAll("[:TZ-]", "") + ".junit.xml");
    }

    @Override
    public void write(TestSummary summary, Logger logger, PrintWriter writer) throws ParserConfigurationException, TransformerException {
        Root root = new Root();
        for (TestSummary.TestInfo testInfo : summary.testInfos()) {
            root.add(testInfo);
        }
        List<TestSuite> suites = new ArrayList<>();
        if (!root.testCases.isEmpty()) {
            TestSuite d = new TestSuite(null, "default");
            root.testCases.values().forEach(d::addToSuite);
            suites.add(d);
        }
        for (TestSuite testSuite : root.children.values()) {
            suites.add(testSuite.copy());
        }
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element testsuites = document.createElement("testsuites");
        testsuites.setAttribute("name", summary.frameworkId().toString());
        testsuites.setAttribute("tests", Integer.toString(root.tests));
        testsuites.setAttribute("failures", Integer.toString(root.failures));
        testsuites.setAttribute("skipped", Integer.toString(root.skipped));
        for (TestSuite suite : suites) {
            testsuites.appendChild(toElement(document, suite));
        }
        document.appendChild(testsuites);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(new DOMSource(document), new StreamResult(writer));
    }

    private Element toElement(Document document, TestSuite suite) {
        Element testsuite = document.createElement("testsuite");
        String path = suite.path();
        testsuite.setAttribute("name", path);
        testsuite.setAttribute("tests", Integer.toString(suite.tests));
        testsuite.setAttribute("failures", Integer.toString(suite.failures));
        testsuite.setAttribute("skipped", Integer.toString(suite.skipped));
        for (TestSuite child : suite.children.values()) {
            testsuite.appendChild(toElement(document, child));
        }
        for (TestCase testCase : suite.testCases.values()) {
            testsuite.appendChild(toElement(document, path, testCase));
        }
        return testsuite;
    }

    private Element toElement(Document document, String path, TestCase testCase) {
        Element testcase = document.createElement("testcase");
        testcase.setAttribute("name", testCase.name);
        testcase.setAttribute("classname", path);
        Element properties = document.createElement("properties");
        Element desc = document.createElement("property");
        desc.setAttribute("name", "description");
        desc.setTextContent("\n" + FormattingUtil.componentsToPlainString(testCase.info.description()) + "\n");
        properties.appendChild(desc);
        Element step = document.createElement("property");
        switch (testCase.type) {
            case FAILURE -> {
                Element failure = document.createElement("failure");
                failure.setAttribute("message", testCase.info.message());
                testcase.appendChild(failure);
                step.setAttribute("name", "step[failure]");
            }
            case SKIPPED -> {
                Element skipped = document.createElement("skipped");
                skipped.setAttribute("message", "Failed but optional: " + testCase.info.message());
                testcase.appendChild(skipped);
                step.setAttribute("name", "step[skipped]");
            }
            case PASSED -> {
                Element passed = document.createElement("passed");
                passed.setAttribute("message", testCase.info.message());
                testcase.appendChild(passed);
                step.setAttribute("name", "step[passed]");
            }
        }
        step.setAttribute("value", testCase.info.message());
        properties.appendChild(step);
        testcase.appendChild(properties);
        return testcase;
    }

    private static class Root extends TestSuite {
        private Root() {
            super(null, "default");
        }

        public void add(TestSummary.TestInfo testInfo) {
            List<String> groups = testInfo.groups();
            if (groups.isEmpty()) {
                addToSuite(testInfo);
            } else {
                for (String group : groups) {
                    addToGroup(group, testInfo);
                }
            }
        }

        private void addToGroup(String groupPath, TestSummary.TestInfo testInfo) {
            String[] parts = groupPath.split("\\.");
            if (parts.length == 0) {
                addToSuite(testInfo);
            } else {
                TestSuite curr = this;
                for (String part : parts) {
                    curr = curr.getOrCreate(part);
                }
                curr.addToSuite(testInfo);
            }
        }
    }

    private static class TestCase {
        final String name;
        final TestSummary.TestInfo info;
        final Type type;

        private TestCase(TestSummary.TestInfo info) {
            this.name = info.testId();
            this.info = info;
            switch (info.result()) {
                case FAILED -> {
                    if (info.required()) {
                        this.type = Type.FAILURE;
                    } else {
                        this.type = Type.SKIPPED;
                    }
                }
                case PASSED -> this.type = Type.PASSED;
                default -> this.type = Type.SKIPPED;
            }
        }

        enum Type {
            PASSED, FAILURE, SKIPPED
        }
    }

    private static class TestSuite {
        final Map<String, TestCase> testCases = new HashMap<>();
        final Map<String, TestSuite> children = new HashMap<>();
        final @Nullable TestSuite parent;
        final String name;
        int tests = 0;
        int failures = 0;
        int skipped = 0;

        private TestSuite(@Nullable TestSuite parent, String name) {
            this.parent = parent;
            this.name = name;
        }

        public TestSuite getOrCreate(String groupName) {
            return children.computeIfAbsent(groupName, name1 -> new TestSuite(this, name1));
        }

        public void addToSuite(TestSummary.TestInfo info) {
            addToSuite(new TestCase(info));
        }

        public void addToSuite(TestCase testCase) {
            onAdd(testCase);
            testCases.put(testCase.name, testCase);
        }

        private void onAdd(TestCase testCase) {
            tests++;
            switch (testCase.type) {
                case FAILURE:
                    failures++;
                    break;
                case SKIPPED:
                    skipped++;
                    break;
            }
            if (parent != null) {
                parent.onAdd(testCase);
            }
        }

        public String path() {
            return parent != null ? parent.path() + "." + name : name;
        }

        public TestSuite copy() {
            TestSuite suite = new TestSuite(null, name);
            suite.testCases.putAll(testCases);
            suite.children.putAll(children);
            suite.tests = tests;
            suite.skipped = skipped;
            suite.failures = failures;
            return suite;
        }
    }
}
