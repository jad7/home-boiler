package com.jad.r4j.boiler.v2.controller;

import com.jad.r4j.boiler.config.Configuration;
import com.jad.r4j.boiler.config.ConfigurationParent;
import com.jad.r4j.boiler.utils.Toggle;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Singleton
public class DisplayService {

    private final BlockingQueue<String> tasks = new ArrayBlockingQueue<>(10);
    private final Configuration configuration;
    private final TMQueueProcess processor = new TMQueueProcess();
    private final File pyFile;
    private final Toggle<Supplier<String>> staticInfo = new Toggle<>();
    private Process process;
    private PrintStream output;

    @Inject
    public DisplayService(Configuration configuration,
                          @Named("config.display.script.path") String path,
                          Lifecycle lifecycle) {
        this.configuration = configuration;
        this.pyFile = new File(path);
        initProcess();
        lifecycle.addDestroyable(this.process::destroy);
        processor.start();
        lifecycle.addDestroyable(processor::interrupt);

    }

    private void initProcess() {
        try {
            File err = new File(Paths.get("").toAbsolutePath().toFile(), "error.ptn");
            if (err.exists()) {
                err.delete();
            }

            err.createNewFile();
            File out = new File(Paths.get("").toAbsolutePath().toFile(), "out.ptn");
            if (out.exists()) {
                out.delete();
            }

            out.createNewFile();

            this.process = (new ProcessBuilder(Arrays.asList("python", pyFile.getAbsolutePath()))).redirectError(err).redirectOutput(ProcessBuilder.Redirect.to(out)).directory(pyFile.getParentFile()).start();
            this.output = new PrintStream(new BufferedOutputStream(this.process.getOutputStream()), true, "UTF-8");
            log.info("Python display ready");
        } catch (IOException var4) {
            throw new RuntimeException("Can not start process", var4);
        }
    }

    public void addStatic(Supplier<String> regularData) {
        staticInfo.add(regularData);
    }

    public void showNotification(String notification) {
        tasks.add(notification);
    }

    private class TMQueueProcess extends Thread {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String poll = tasks.poll(10, TimeUnit.SECONDS);
                    if (poll != null) {
                        send(poll);
                    } else {
                        send(staticInfo.next().get());
                    }
                } catch (InterruptedException e) {
                    return;
                }
            }
        }

        private void send(String str) throws InterruptedException {
            if (!process.isAlive()) {
                log.error("Python display process is dead");
                initProcess();
            }

            if (ConfigurationParent.debug) {
                log.info("Sent to display: \"{}\"", str);
            }

            if (str.length() <= 4) {
                long time = System.currentTimeMillis();
                output.println("any " + str);
                output.flush();
                Thread.sleep(3000 - (System.currentTimeMillis() - time));
            } else {
                output.println("any " + str);
                output.flush();
            }
        }
    }


}
