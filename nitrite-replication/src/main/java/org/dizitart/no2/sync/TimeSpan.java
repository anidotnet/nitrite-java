package org.dizitart.no2.sync;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.concurrent.TimeUnit;

/**
 * @author Anindya Chatterjee
 */
@Data
@AllArgsConstructor
public class TimeSpan {
    private long time;
    private TimeUnit timeUnit;
}