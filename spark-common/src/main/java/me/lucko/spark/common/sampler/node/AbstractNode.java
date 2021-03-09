/*
 * This file is part of spark.
 *
 *  Copyright (C) Albert Pham <http://www.sk89q.com>
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.lucko.spark.common.sampler.node;

import me.lucko.spark.common.sampler.async.AsyncStackTraceElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Encapsulates a timed node in the sampling stack.
 */
public abstract class AbstractNode {

    private static final int MAX_STACK_DEPTH = 300;

    /**
     * A map of this nodes children
     */
    private final Map<StackTraceNode.Description, StackTraceNode> children = new ConcurrentHashMap<>();

    /**
     * The accumulated sample time for this node, measured in microseconds
     */
    private final LongAdder totalTime = new LongAdder();

    /**
     * Returns the total sample time for this node in milliseconds.
     *
     * @return the total time
     */
    public double getTotalTime() {
        return this.totalTime.longValue() / 1000d;
    }

    /**
     * Merge {@code other} into {@code this}.
     *
     * @param other the other node
     */
    public void merge(AbstractNode other) {
        this.totalTime.add(other.totalTime.longValue());
        for (Map.Entry<StackTraceNode.Description, StackTraceNode> child : other.children.entrySet()) {
            resolveChild(child.getKey()).merge(child.getValue());
        }
    }

    private AbstractNode resolveChild(StackTraceNode.Description description) {
        StackTraceNode result = this.children.get(description); // fast path
        if (result != null) {
            return result;
        }
        return this.children.computeIfAbsent(description, name -> new StackTraceNode(description));
    }

    public void log(StackTraceElement[] elements, long time) {
        log(elements, 0, time);
    }
    
    private void log(StackTraceElement[] elements, int offset, long time) {
        this.totalTime.add(time);

        if (offset >= MAX_STACK_DEPTH) {
            return;
        }
        
        if (elements.length - offset == 0) {
            return;
        }

        // the first element in the array is the top of the call stack, and the last is the root
        // offset starts at 0.

        // pointer is determined by subtracting the offset from the index of the last element
        int pointer = (elements.length - 1) - offset;
        StackTraceElement element = elements[pointer];

        // the parent stack element is located at pointer+1.
        // when the current offset is 0, we know the current pointer is at the last element in the
        // array (the root) and therefore there is no parent.
        StackTraceElement parent = offset == 0 ? null : elements[pointer + 1];

        // get the line number of the parent element - the line which called "us"
        int parentLineNumber = parent == null ? StackTraceNode.NULL_LINE_NUMBER : parent.getLineNumber();

        // resolve a child element within the structure for the element at pointer
        AbstractNode child = resolveChild(new StackTraceNode.Description(element.getClassName(), element.getMethodName(), element.getLineNumber(), parentLineNumber));
        // call the log method on the found child, with an incremented offset.
        child.log(elements, offset + 1, time);
    }

    public void log(AsyncStackTraceElement[] elements, long time) {
        log(elements, 0, time);
    }

    private void log(AsyncStackTraceElement[] elements, int offset, long time) {
        this.totalTime.add(time);

        if (offset >= MAX_STACK_DEPTH) {
            return;
        }

        if (elements.length - offset == 0) {
            return;
        }

        // the first element in the array is the top of the call stack, and the last is the root
        // offset starts at 0.

        // pointer is determined by subtracting the offset from the index of the last element
        int pointer = (elements.length - 1) - offset;
        AsyncStackTraceElement element = elements[pointer];

        // resolve a child element within the structure for the element at pointer
        AbstractNode child = resolveChild(new StackTraceNode.Description(element.getClassName(), element.getMethodName(), element.getMethodDescription()));
        // call the log method on the found child, with an incremented offset.
        child.log(elements, offset + 1, time);
    }

    protected List<StackTraceNode> exportChildren(MergeMode mergeMode) {
        if (this.children.isEmpty()) {
            return Collections.emptyList();
        }

        List<StackTraceNode> list = new ArrayList<>(this.children.size());

        outer:
        for (StackTraceNode child : this.children.values()) {
            // attempt to find an existing node we can merge into
            for (StackTraceNode other : list) {
                if (mergeMode.shouldMerge(other, child)) {
                    other.merge(child);
                    continue outer;
                }
            }

            // just add
            list.add(child);
        }

        list.sort(null);
        return list;
    }

}
