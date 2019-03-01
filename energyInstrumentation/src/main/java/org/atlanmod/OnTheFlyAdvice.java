package org.atlanmod;

import net.bytebuddy.asm.Advice;
import org.powerapi.PowerDisplay;
import org.powerapi.core.power.Power;
import org.powerapi.core.target.Target;
import scala.Long;
import scala.collection.immutable.Set;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class OnTheFlyAdvice {

    @Advice.OnMethodEnter
    static void enter(@Advice.Local("monitor") Monitor monitor) {
        ArrayList<Double> list = new ArrayList<Double>();
        String sPath = "./energyInstrumentation/src/main/resources/outputTER.txt";
        try {
            Files.delete(Paths.get(sPath));
        } catch (Exception e) {
        }
        try {
            Files.createFile(Paths.get(sPath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        PowerDisplay display = new PowerDisplay() {
            @Override
            public void display(UUID muid, long timestamp, Set<Target> targets, Set<String> devices, Power power) {
                String s = power.toMilliWatts() + ";";
                try {
                    Files.write(Paths.get(sPath), s.getBytes(), StandardOpenOption.APPEND);
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        monitor = new MonitorBuilder()
                .withDuration(60, TimeUnit.SECONDS)
                .withRefreshFrequency(100, TimeUnit.MILLISECONDS)
                .withTdp(15)
                .withTdpFactor(0.7)
                .withCustomDisplay(display)
                .build();
        monitor.run((int) SystemUtils.getPID());
    }

    @Advice.OnMethodExit
    static void exit(@Advice.Local("monitor") Monitor monitor) {

        monitor.stop();
    }
}