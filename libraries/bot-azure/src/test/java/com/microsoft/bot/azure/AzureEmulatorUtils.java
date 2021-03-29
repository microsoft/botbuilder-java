package com.microsoft.bot.azure;

import java.io.File;
import java.io.IOException;

public class AzureEmulatorUtils {

    private static boolean isStorageEmulatorAvailable = false;
    private static boolean hasStorageEmulatorBeenTested = false;
    private static final String NO_EMULATOR_MESSAGE = "This test requires Azure STORAGE Emulator! Go to https://docs.microsoft.com/azure/storage/common/storage-use-emulator to download and install.";

    public static boolean isStorageEmulatorAvailable() {
        if (!hasStorageEmulatorBeenTested) {
            try {
                File emulator = new File(System.getenv("ProgramFiles") + " (x86)\\Microsoft SDKs\\Azure\\Storage Emulator\\AzureStorageEmulator.exe");
                if (emulator.exists()) {
                    Process p = Runtime.getRuntime().exec("cmd /C \"" + System.getenv("ProgramFiles")
                            + " (x86)\\Microsoft SDKs\\Azure\\Storage Emulator\\AzureStorageEmulator.exe\" start");
                    int result = p.waitFor();
                    // status = 0: the service was started.
                    // status = -5: the service is already started. Only one instance of the
                    // application
                    // can be run at the same time.
                    isStorageEmulatorAvailable = result == 0 || result == -5;
                    } else {
                        isStorageEmulatorAvailable = false;
                    }
            } catch (IOException | InterruptedException ex) {
                isStorageEmulatorAvailable = false;
                System.out.println(NO_EMULATOR_MESSAGE);
            }
            hasStorageEmulatorBeenTested = true;
        }
        return isStorageEmulatorAvailable;
    }
}
