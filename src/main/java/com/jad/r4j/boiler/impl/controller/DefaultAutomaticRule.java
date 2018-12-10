package com.jad.r4j.boiler.impl.controller;

/**

 * <p>
 * Developed by Grid Dynamics International, Inc. for the customer Art.com.
 * http://www.griddynamics.com
 * <p>
 * Classification level: Confidential
 * <p>
 * EXCEPT EXPRESSED BY WRITTEN WRITING, THIS CODE AND INFORMATION ARE PROVIDED "AS IS"
 * WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * For information about the licensing and copyright of this document please
 * contact Grid Dynamics at info@griddynamics.com.
 *
 * @since 11/25/2018
 */
public class DefaultAutomaticRule implements Rule {

    @Override
    public boolean isAppliableRightNowForChanging(Type type, State state) {
        return true;
    }

    @Override
    public void changeState(Type type, State state) {
        if (type == Type.BOILER) {
            /*boolean atHome = getBoilerMode();


            if (sensorsProvider.isBoilerOff()) {
                if (currentTemp <= holder.minWhenNotAtHomeTemperature()
                        || (anyAtHome && currentTemp <= holder.minWhenAtHomeTemperature())) {
                    log.info("Home temperature {} at home: {} boiler switched to ON", currentTemp, anyAtHome);
                    sensorsProvider.boilerTurnOn();
                }
            } else {
                if ((anyAtHome && currentTemp >= holder.maxWhenAtHomeTemperature())
                        || (!anyAtHome && currentTemp >= holder.maxWhenNotAtHomeTemperature())) {
                    log.info("Home temperature {} at home: {} boiler switched to OFF", currentTemp, anyAtHome);
                    sensorsProvider.boilerTurnOff();
                }
            }*/
        }

    }
}
