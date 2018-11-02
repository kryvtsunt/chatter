package edu.northeastern.ccs.im;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NextDoesNotExistExceptionTest {

    @Test
    public void exception(){
        try{
            NextDoesNotExistException exception = new NextDoesNotExistException("No next!");
        } catch(NextDoesNotExistException e){
            assertEquals("No next!", e.getMessage());
        }
    }

}