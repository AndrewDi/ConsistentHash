package com.consistenthash;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Test implements ITest {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    public int add(){
        return 3;
    }
}
