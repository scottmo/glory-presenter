package com.scottmo.shared;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.List;

class PairTest {

    @Test
    void of_createsPair() {
        Pair<String, Integer> pair = Pair.of("key", 42);
        assertEquals("key", pair.key());
        assertEquals(42, pair.value());
    }

    @Test
    void ofList_createsPairsFromVarargs() {
        List<Pair<String, Integer>> pairs = Pair.ofList("a", 1, "b", 2);
        assertEquals(2, pairs.size());
        assertEquals("a", pairs.get(0).key());
        assertEquals(1, pairs.get(0).value());
        assertEquals("b", pairs.get(1).key());
        assertEquals(2, pairs.get(1).value());
    }

    @Test
    void ofList_throwsForOddArguments() {
        assertThrows(IllegalArgumentException.class, () -> {
            Pair.ofList("a", 1, "b");
        });
    }

    @Test
    void ofList_returnsEmptyListForNoArgs() {
        List<Pair<String, Integer>> pairs = Pair.ofList();
        assertTrue(pairs.isEmpty());
    }

    @Test
    void pair_equality() {
        Pair<String, Integer> pair1 = Pair.of("key", 42);
        Pair<String, Integer> pair2 = Pair.of("key", 42);
        assertEquals(pair1, pair2);
    }
}

