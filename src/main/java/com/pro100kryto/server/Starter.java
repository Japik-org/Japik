package com.pro100kryto.server;

import com.pro100kryto.server.extension.ExtensionLoader;
import com.pro100kryto.server.extension.IExtension;
import com.pro100kryto.server.logger.ILogger;
import org.apache.commons.cli.*;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Paths;

public final class Starter {
    private static Server server;

    @Nullable
    public static Server getServer() {
        return server;
    }

    public static void main(String[] args) throws Throwable {
        Starter.server = new Server(
                Paths.get(System.getProperty("user.dir"))
        );
        final ILogger logger = server.getLoggerManager().getMainLogger();

        final Options options = new Options();

        options.addOption(new Option("e", "ext-add-start", true, "add and start extension"));
        options.addOption(new Option("a", "ext-add", true, "add extension"));
        options.addOption(new Option("s", "start", false, "start server"));

        final CommandLineParser parser = new BasicParser();
        final HelpFormatter formatter = new HelpFormatter();

        try {
            final CommandLine line = parser.parse(options, args);

            try {
                server.getLiveCycle().init();
            } catch (Throwable throwable){
                logger.exception(throwable, "Failed init server");
                throw throwable;
            }

            if (line.hasOption("start")){
                try {
                    server.getLiveCycle().start();
                } catch (Throwable throwable){
                    logger.exception(throwable, "Failed start server");
                }
            }

            if (line.hasOption("ext-add")){
                final String[] extTypes = line.getOptionValues("ext-add");
                final ExtensionLoader extensionLoader = server.getExtensionLoader();

                for(final String extType: extTypes){
                    try {
                        final IExtension<?> extension = extensionLoader.load(extType, extType, null);
                    } catch (Throwable e) {
                        logger.exception(e, "Failed add extension");
                    }
                }
            }

            if (line.hasOption("ext-add-start")){
                final String[] extTypes = line.getOptionValues("ext-add-start");
                final ExtensionLoader extensionCreator = server.getExtensionLoader();

                for(final String extType: extTypes){
                    final IExtension<?> extension;
                    try {
                        extension = extensionCreator.load(extType, extType, null);
                    } catch (Throwable e) {
                        logger.exception(e, "Failed add extension");
                        continue;
                    }
                    try{
                        extension.getLiveCycle().start();
                    } catch (Throwable e){
                        logger.exception(e, "Failed start extension");
                    }
                }
            }

        } catch (ParseException parseException) {
            logger.exception(parseException);
            formatter.printHelp("starter", options);
            throw parseException;

        } catch (Throwable throwable) {
            logger.exception(throwable, "Unexpected error occurred");
            throw throwable;
        }
    }
}
