/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

/**
 * Mod pre-loading and pre-adaptation support.
 *
 * <p>This package adds observability, caching and compatibility checking around the mod-loading
 * pipeline without changing its ordering guarantees. It is split into small, single-purpose
 * modules so the loading flow stays readable and each piece is independently testable:</p>
 *
 * <table>
 * <caption>Module overview</caption>
 * <tr><th>Module</th><th>Responsibility</th></tr>
 * <tr><td>{@code perf}</td><td>Phase timing and the {@code --perf} report (PR-LOAD-6).</td></tr>
 * <tr><td>{@code cache}</td><td>Persistent per-file mod index keyed by file fingerprint (PR-LOAD-1/3).</td></tr>
 * <tr><td>{@code adapt}</td><td>Compatibility precheck: dependency rules, symbol scanning and the
 * breaking-changes database (PR-ADAPT-1/2).</td></tr>
 * <tr><td>{@code diagnostics}</td><td>Structured diagnostic bundle on load failure (PR-X-6).</td></tr>
 * </table>
 *
 * <p>{@link net.neoforged.neoforge.loading.LoadingConfig} supplies the per-install switches; the
 * pipeline itself is driven from {@code CommonModLoader}, which times each stage and runs the
 * precheck between mod initialization and event dispatch.</p>
 */
@NullMarked
package net.neoforged.neoforge.loading;

import org.jspecify.annotations.NullMarked;
