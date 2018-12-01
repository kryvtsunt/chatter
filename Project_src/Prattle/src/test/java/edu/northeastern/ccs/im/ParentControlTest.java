package edu.northeastern.ccs.im;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
class ParentControlTest {

    @Mock
    private BufferedReader bufferedReader;

    @Test
    void filterBadWords() {
//        ParentControl pc = ParentControl.getInstance();
//        assertEquals("***", pc.filterBadWords("ass"));
//        pc.getInstance();

    }

}