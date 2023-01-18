package com.template.contracts;

import com.template.states.ManagementState;
import org.junit.Test;

public class StateTests {

    //Mock State test check for if the state has correct parameters type
    @Test
    public void hasFieldOfCorrectType() throws NoSuchFieldException {
        ManagementState.class.getDeclaredField("msg");
        assert (ManagementState.class.getDeclaredField("msg").getType().equals(String.class));
    }
}