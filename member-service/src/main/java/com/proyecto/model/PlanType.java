package com.proyecto.model;

public enum PlanType {
	    MENSUAL(1),
	    TRIMESTRAL(3),
	    ANUAL(12);

	    private final int months;

	    PlanType(int months) {
	        this.months = months;
	    }

	    public int getMonths() {
	        return months;
	    }
}
