/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber,
 *                             Ralph Germ, Martin Budden
 *                             <info@movsim.org>
 * ----------------------------------------------------------------------
 * 
 *  This file is part of 
 *  
 *  MovSim - the multi-model open-source vehicular-traffic simulator 
 *
 *  MovSim is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MovSim is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MovSim.  If not, see <http://www.gnu.org/licenses/> or
 *  <http://www.movsim.org>.
 *  
 * ----------------------------------------------------------------------
 */
package org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl;

import org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataGipps;
import org.movsim.simulator.vehicles.Moveable;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModelCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
// paper reference and modifications ...

/**
 * The Class Gipps.
 */
public class Gipps extends AccelerationModelAbstract implements AccelerationModel {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(Gipps.class);

    /**
     * The T. results from update timestep dt dt = T = Tr = tau_relax
     */
    private double T;

    /** The v0. */
    private double v0;

    /** The a. */
    private double a;

    /** The b. */
    private double b;

    /** The s0. */
    private double s0;

    /**
     * Instantiates a new gipps.
     * 
     * @param modelName
     *            the model name
     * @param parameters
     *            the parameters
     */
    public Gipps(String modelName, AccelerationModelInputDataGipps parameters) {
        super(modelName, AccelerationModelCategory.INTERATED_MAP_MODEL, parameters);
        initParameters();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl.
     * LongitudinalModel#initParameters()
     */
    @Override
    protected void initParameters() {
        logger.debug("init model parameters");
        this.T = ((AccelerationModelInputDataGipps) parameters).getDt();
        this.v0 = ((AccelerationModelInputDataGipps) parameters).getV0();
        this.a = ((AccelerationModelInputDataGipps) parameters).getA();
        this.b = ((AccelerationModelInputDataGipps) parameters).getB();
        this.s0 = ((AccelerationModelInputDataGipps) parameters).getS0();
    }

    /**
     * Gets the t.
     * 
     * @return the t
     */
    public double getT() {
        return T;
    }

    /**
     * Gets the v0.
     * 
     * @return the v0
     */
    public double getV0() {
        return v0;
    }

    /**
     * Gets the a.
     * 
     * @return the a
     */
    public double getA() {
        return a;
    }

    /**
     * Gets the b.
     * 
     * @return the b
     */
    public double getB() {
        return b;
    }

    /**
     * Gets the s0.
     * 
     * @return the s0
     */
    public double getS0() {
        return s0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel
     * #acc(org.movsim.simulator.vehicles.Vehicle,
     * org.movsim.simulator.vehicles.VehicleContainer, double, double, double)
     */
    @Override
    public double calcAcc(Vehicle me, VehicleContainer vehContainer, double alphaT, double alphaV0, double alphaA) {

        // Local dynamical variables
        final Moveable vehFront = vehContainer.getLeader(me);
        final double s = me.getNetDistance(vehFront);
        final double v = me.getSpeed();
        final double dv = (vehFront == null) ? 0 : v - vehFront.getSpeed();

        // space dependencies modeled by speedlimits, alpha's

        // consider external speedlimit
        final double v0Local = Math.min(alphaV0 * v0, me.getSpeedlimit());

        // #############################################################
        // space dependencies modelled by alpha_T
        // (!!! watch for alpha_T: dt unchanged, possibly inconsistent!)
        // #############################################################

        final double TLocal = alphaT * T;

        // actual Gipps formula
        return acc(s, v, dv, v0Local, TLocal);

    }

    @Override
    public double calcAcc(final Vehicle me, final Vehicle vehFront){
        // Local dynamical variables
        final double s = me.getNetDistance(vehFront);
        final double v = me.getSpeed();
        final double dv = me.getRelSpeed(vehFront);
        
        final double TLocal = T;
        final double v0Local =  Math.min(v0, me.getSpeedlimit());

        return acc(s, v, dv, v0Local, TLocal);
    }

    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel
     * #accSimple(double, double, double)
     */
    @Override
    public double calcAccSimple(double s, double v, double dv) {
        return acc(s, v, dv, v0, T);
    }

    /**
     * Acc.
     * 
     * @param s
     *            the s
     * @param v
     *            the v
     * @param dv
     *            the dv
     * @param v0Local
     *            the v0 local
     * @param TLocal
     *            the t local
     * @return the double
     */
    private double acc(double s, double v, double dv, double v0Local, double TLocal) {
        final double vp = v - dv;
        // safe speed
        final double vSafe = -b * T + Math.sqrt(b * b * T * T + vp * vp + 2 * b * Math.max(s - s0, 0.));
        final double vNew = Math.min(vSafe, Math.min(v + a * TLocal, v0Local));
        final double aWanted = (vNew - v) / T;
        return aWanted;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl.
     * LongitudinalModel#parameterV0()
     */
    @Override
    public double getDesiredSpeedParameterV0() {
        return v0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl.
     * LongitudinalModel#getRequiredUpdateTime()
     */
    @Override
    public double getRequiredUpdateTime() {
        return this.T; // iterated map requires specific timestep!!
    }

    @Override
    protected void setDesiredSpeedV0(double v0) {
        this.v0 = v0;
    }

}
