package com.anonymous.metrics;


public interface MetricCalculatorBase
{
    void calculate(MetricCalculationResults results);

    @Override
    String toString();
}
