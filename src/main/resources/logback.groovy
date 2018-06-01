//
// Built on Fri Jun 01 12:59:46 CEST 2018 by logback-translator
// For more information on configuration files in Groovy
// please see http://logback.qos.ch/manual/groovy.html

// For assistance related to this tool or configuration files
// in general, please contact the logback user mailing list at
//    http://qos.ch/mailman/listinfo/logback-user

// For professional support please see
//   http://www.qos.ch/shop/products/professionalSupport

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.turbo.MarkerFilter

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} [%-21thread] %-5level %-20logger{20} | %msg%n"
    }
}

turboFilter(MarkerFilter) {
    name = "UDP Tunnel Filter"
    marker = "UDP_TUNNEL"
    onMatch = "DENY"
}

root(TRACE, ["STDOUT"])
