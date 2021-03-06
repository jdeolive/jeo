package org.jeo.cli.cmd;

import java.util.Iterator;

import jline.console.ConsoleReader;

import org.jeo.cli.JeoCLI;
import org.jeo.data.Driver;
import org.jeo.data.Drivers;

import com.beust.jcommander.Parameters;

@Parameters(commandNames="drivers", commandDescription="Lists avialable format drivers")
public class DriversCmd extends JeoCmd {

    @Override
    protected void doCommand(JeoCLI cli) throws Exception {
        ConsoleReader console = cli.getConsole();

        Iterator<Driver<?>> it = Drivers.list();
        console.println();
        console.println("Drivers:");
        console.println();
        while(it.hasNext()) {
            console.print("\t");
            console.println(it.next().getName());
        }
    }

}
