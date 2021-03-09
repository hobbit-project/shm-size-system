package org.hobbit.sdk.examples.examplebenchmark.system;

import java.io.InputStream;
import org.hobbit.core.components.AbstractSystemAdapter;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.sdk.examples.examplebenchmark.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Supplier;


public class SystemAdapter extends AbstractSystemAdapter implements Supplier<String> {
    private static final Logger logger = LoggerFactory.getLogger(SystemAdapter.class);

    @Override
    public void receiveGeneratedData(byte[] data) {
    }

    @Override
    public void receiveGeneratedTask(String taskId, byte[] data) {
    }

    public void receiveCommand(byte command, byte[] data) {
        if (command == Constants.INPUT_DATA_COMMAND) {
            String input = RabbitMQUtils.readString(data);
            logger.info("Input (discarded): {}", input);
            String output = get();
            if (output != null) {
                logger.info("Output: {}", output);
                try {
                    sendToCmdQueue(Constants.OUTPUT_DATA_COMMAND, RabbitMQUtils.writeString(output));
                    terminate(null);
                } catch (IOException e) {
                    terminate(e);
                }
            }
        } else {
            super.receiveCommand(command, data);
        }
    }

    @Override
    public String get() {
        try {
            // Process process = new ProcessBuilder("sh", "-c", "df --output=size --block-size=1 /dev/shm |awk '$N>1'").start();
            Process process = new ProcessBuilder("sh", "-c", "df --output=size -h /dev/shm |tail -n +2").start();

            InputStream input = process.getInputStream();
            StringBuilder builder = new StringBuilder();
            byte[] buffer = new byte[1024];
            int read;
            while((read = input.read(buffer)) != -1) {
                String output = new String(buffer, 0, read);
                builder.append(output);
            };
            input.close();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                logger.error("Subprocess returned exit code {}", exitCode);
                terminate(new RuntimeException("Subprocess error"));
                return null;
            }

            return builder.toString();
        } catch (IOException | InterruptedException e) {
            terminate(e);
            return null;
        }
    }

}

