package edu.northeastern.ccs.im;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;



class ParentControlTest {


    @Test
    void filterBadWords() {
        ParentControl pc = ParentControl.getInstance();
        assertEquals("***", pc.filterBadWords("ass"));
    }

}