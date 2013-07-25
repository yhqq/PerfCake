package org.perfcake.nreporting.destinations;

import org.perfcake.nreporting.Measurement;
import org.perfcake.nreporting.ReportingException;

/**
 * @author Pavel Macík <pavel.macik@gmail.com>
 * 
 */
public class DummyDestination implements Destination {

   private String property = null;

   /*
    * (non-Javadoc)
    * 
    * @see org.perfcake.nreporting.destinations.Destination#open()
    */
   @Override
   public void open() {
      // nop
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.perfcake.nreporting.destinations.Destination#close()
    */
   @Override
   public void close() {
      // nop
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.perfcake.nreporting.destinations.Destination#report(org.perfcake.nreporting.Measurement)
    */
   @Override
   public void report(Measurement m) throws ReportingException {
      System.out.println(m.toString());
   }

   /**
    * Used to read the value of property.
    * 
    * @return The property value.
    */
   public String getProperty() {
      return property;
   }

   /**
    * Used to set the value of property.
    * 
    * @param property
    *           The property value to set.
    */
   public void setProperty(String property) {
      this.property = property;
   }

}
