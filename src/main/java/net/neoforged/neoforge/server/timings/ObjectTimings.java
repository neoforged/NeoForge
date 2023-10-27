/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.timings;

import java.lang.ref.WeakReference;

/**
 * ObjectTimings aggregates timings data collected by {@link TimeTracker} for an Object
 * and performs operations for interpretation of the data.
 *
 * @param <T>
 */
public class ObjectTimings<T>
{

    private WeakReference<T> object;

    private int[] rawTimingData;

    public ObjectTimings(T object, int[] rawTimingData)
    {
        this.object = new WeakReference<T>(object);
        this.rawTimingData = rawTimingData;
    }

    /**
     * Retrieves the object that the timings are for
     *
     * @return The object
     */
    public WeakReference<T> getObject()
    {
        return object;
    }


    /**
     * Averages the raw timings data collected
     *
     * @return An average of the raw timing data
     */
    public double getAverageTimings()
    {
        double sum = 0.0;

        for (int data : rawTimingData)
        {
            sum += data;
        }

        return sum / rawTimingData.length;
    }
}
