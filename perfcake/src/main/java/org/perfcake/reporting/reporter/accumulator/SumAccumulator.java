/*
 * -----------------------------------------------------------------------\
 * PerfCake
 *  
 * Copyright (C) 2010 - 2016 the original author or authors.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -----------------------------------------------------------------------/
 */
package org.perfcake.reporting.reporter.accumulator;

import java.util.concurrent.atomic.DoubleAdder;

/**
 * Accumulates the sum of values.
 *
 * @author <a href="mailto:pavel.macik@gmail.com">Pavel Macík</a>
 */
public class SumAccumulator implements Accumulator<Double> {

   /**
    * Sum of the reported values.
    */
   private DoubleAdder sum = new DoubleAdder();

   @Override
   public void add(final Double number) {
      sum.add(number);
   }

   @Override
   public Double getResult() {
      return sum.doubleValue();
   }

   @Override
   public void reset() {
      sum = new DoubleAdder();
   }
}
