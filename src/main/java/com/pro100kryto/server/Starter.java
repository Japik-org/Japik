package com.pro100kryto.server;

import com.pro100kryto.server.extension.ExtensionLoader;
import com.pro100kryto.server.extension.IExtension;
import com.pro100kryto.server.logger.ILogger;
import org.apache.commons.cli.*;

public final class Starter {

    public static void main(String[] args) {
        final IServerControl serverControl = Server.createNewInstance();
        final ILogger logger = serverControl.getLoggerManager().getMainLogger();

        final Options options = new Options();

        options.addOption(new Option("e", "ext-add-start", true, "add and start extension"));
        options.addOption(new Option("a", "ext-add", true, "add extension"));
        options.addOption(new Option("s", "start", false, "start server"));

        final CommandLineParser parser = new DefaultParser();
        final HelpFormatter formatter = new HelpFormatter();

        try {
            final CommandLine line = parser.parse(options, args);

            if (line.hasOption("start")){
                try {
                    serverControl.start();
                } catch (Throwable throwable){
                    logger.writeException(throwable, "Failed start server");
                }
            }

            if (line.hasOption("ext-add")){
                final String[] values = line.getOptionValues("ext-add");
                final ExtensionLoader extensionCreator = serverControl.getExtensionCreator();

                for(String val: values){
                    try {
                        IExtension extension = extensionCreator.create(val);
                        serverControl.addExtension(extension);
                    } catch (Throwable e) {
                        logger.writeException(e, "Failed add extension");
                    }
                }
            }

            if (line.hasOption("ext-add-start")){
                final String[] values = line.getOptionValues("ext-add-start");
                final ExtensionLoader extensionCreator = serverControl.getExtensionCreator();

                for(String val: values){
                    try {
                        IExtension extension = extensionCreator.create(val);
                        serverControl.addExtension(extension);
                    } catch (Throwable e) {
                        logger.writeException(e, "Failed add extension");
                        continue;
                    }
                    try{
                        serverControl.getExtension(val).start();
                    } catch (Throwable e){
                        logger.writeException(e, "Failed start extension");
                    }
                }
            }

        } catch (ParseException e) {
            logger.writeException(e);
            formatter.printHelp("starter", options);
            System.exit(1);

        } catch (Throwable throwable) {
            logger.writeException(throwable, "Unexpected error occurred");
            System.exit(1);
        }
    }
}
