package com.japik;

import com.japik.extension.ExtensionLoader;
import com.japik.extension.IExtension;
import com.japik.logger.ILogger;
import org.apache.commons.cli.*;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Paths;

public final class Starter {
    private static Japik server;

    @Nullable
    public static Japik getServer() {
        return server;
    }

    public static void main(String[] args) throws Throwable {
        Starter.server = new Japik(
                Paths.get(System.getProperty("user.dir"))
        );
        final ILogger logger = server.getLoggerManager().getMainLogger();

        final Options options = new Options();

        options.addOption(new Option("a", "ext-add", true, "add extension"));
        options.addOption(new Option("e", "ext-start", true, "start extension"));
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

            if (line.hasOption(options.getOption("s"))){
                try {
                    server.getLiveCycle().start();
                } catch (Throwable throwable){
                    logger.exception(throwable, "Failed start server");
                }
            }

            {
                final Option option = options.getOption("a");
                if (line.hasOption(option)) {
                    final String[] extTypes = line.getOptionValues(option);
                    final ExtensionLoader extensionLoader = server.getExtensionLoader();

                    for (final String extType : extTypes) {
                        try {
                            final IExtension<?> extension = extensionLoader.load(extType, extType, null);
                        } catch (Throwable e) {
                            logger.exception(e, "Failed add extension");
                        }
                    }
                }
            }

            {
                final Option option = options.getOption("e");
                if (line.hasOption(option)) {
                    final String[] extTypes = line.getOptionValues(option);
                    final ExtensionLoader extensionLoader = server.getExtensionLoader();

                    for (final String extType : extTypes) {
                        final IExtension<?> extension = extensionLoader.get(extType);
                        try {
                            extension.getLiveCycle().start();
                        } catch (Throwable e) {
                            logger.exception(e, "Failed start extension");
                        }
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
