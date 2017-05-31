package com.citytransportanalysis.gui;

import com.citytransportanalysis.modeling.Modeling;
import com.citytransportanalysis.modeling.entity.Transport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.*;

/**
 * Created by Masha on 5/30/2017.
 */
public class ModelingTest {

    Modeling modeling = new Modeling();

    @Test
    public void testStartModellingButtonClicked() throws Exception {
        List<Transport> res = modeling.transportData(-1,0,-8, LocalTime.now(), 1);
        System.out.println(res);
        List<Transport> expected = new ArrayList<>();
        assertArrayEquals(res.toArray(), expected.toArray());
    }

    @Test(expected = NullPointerException.class)
    public void testTransportDataWithValidDAta() throws Exception {
        List<Transport> res = modeling.transportData(-9,-6,0, null, 0);
        System.out.println(res);
        List<Transport> expected = new ArrayList<>();
        assertArrayEquals(res.toArray(), expected.toArray());
    }
    @Test(expected = RuntimeException.class)
    public void testTransportDataWithValidPeriod() throws Exception {
        List<Transport> res = modeling.transportData(-9,-6,0, LocalTime.now(), -8);
        System.out.println(res);
        List<Transport> expected = new ArrayList<>();
        assertArrayEquals(res.toArray(), expected.toArray());
    }
}