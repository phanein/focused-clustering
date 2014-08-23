/*
 * Copyright (c) 2012, Søren Atmakuri Davidsen
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.stonybrook.focused.io.graphml;

import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.VertexNameProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Soren A. Davidsen <soren@tanesha.net>
 */
public class ContinousNumericIDProviders {

    public static class ContinousNumericVertexNameProvider<V> implements VertexNameProvider<V> {
        private Map<V, Integer> map = new HashMap<V, Integer>();
        private int i = 0;
        @Override
        public String getVertexName(V v) {
            if (!map.containsKey(v))
                map.put(v, i++);

            return "n" + map.get(v);
        }
    }

    public static class ContinousNumericEdgeNameProvider<E> implements EdgeNameProvider<E> {
        private Map<E, Integer> map = new HashMap<E, Integer>();
        private int i = 0;
        @Override
        public String getEdgeName(E e) {
            if (!map.containsKey(e))
                map.put(e, i++);

            return "e" + map.get(e);
        }
    }

    public static class IntegerVertexNameProvider<Integer> implements VertexNameProvider<Integer> {
        @Override
        public String getVertexName(Integer v) {
            return "n" + v;
        }
    }
}
