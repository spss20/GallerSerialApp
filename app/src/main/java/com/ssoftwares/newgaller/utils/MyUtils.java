package com.ssoftwares.newgaller.utils;

public class MyUtils {

    private static final String HEXES = "0123456789ABCDEF";
    private static final String HEX_INDICATOR = "0x";
    private static final String SPACE = " ";


    public static byte generateChecksum(byte[] array) {
        byte output = 0;
        for (int i = 0; i < array.length; i++) {
            output = (byte) (output ^ array[i]);
        }
        byte endByte = 127;
        output = (byte) (output & endByte);
        byte one = 1;
        output = (byte) (output + one);
        return output;
    }

    public static byte[] stringTobytes(String hexString) {
        String stringProcessed = hexString.trim().replaceAll("0x", "");
        stringProcessed = stringProcessed.replaceAll("\\s+", "");
        byte[] data = new byte[stringProcessed.length() / 2];
        if (stringProcessed.length() % 2 != 0) {
            return null;
        }
        int i = 0;
        int j = 0;
        while (i <= stringProcessed.length() - 1) {
            byte character = (byte) Integer.parseInt(stringProcessed.substring(i, i + 2), 16);
            data[j] = character;
            j++;
            i += 2;
        }
        return data;
    }

    public static String hexToString(byte[] data) {
        if (data != null) {
            StringBuilder hex = new StringBuilder(2 * data.length);
            for (int i = 0; i <= data.length - 1; i++) {
                byte dataAtIndex = data[i];
//                hex.append(HEX_INDICATOR);
                hex.append(HEXES.charAt((dataAtIndex & 0xF0) >> 4))
                        .append(HEXES.charAt((dataAtIndex & 0x0F)));
                hex.append(SPACE);
            }
            return hex.toString();
        } else {
            return null;
        }
    }

    public static int getHexInteger(byte[] data) {
        if (data != null) {
            StringBuilder hex = new StringBuilder(2 * data.length);
            for (int i = 0; i <= data.length - 1; i++) {
                byte dataAtIndex = data[i];
                hex.append(HEXES.charAt((dataAtIndex & 0xF0) >> 4))
                        .append(HEXES.charAt((dataAtIndex & 0x0F)));
            }
            return Integer.parseInt(hex.toString(), 16);
        } else {
            return 0;
        }
    }

    public static long getHexLong(byte[] data) {
        if (data != null) {
            StringBuilder hex = new StringBuilder(2 * data.length);
            for (int i = 0; i <= data.length - 1; i++) {
                byte dataAtIndex = data[i];
                hex.append(HEXES.charAt((dataAtIndex & 0xF0) >> 4))
                        .append(HEXES.charAt((dataAtIndex & 0x0F)));
            }
            return Long.parseLong(hex.toString(), 16);
        } else {
            return 0;
        }
    }

    public static String formatIp(byte[] remoteIp) {
        StringBuilder ip = new StringBuilder();
        for (byte i : remoteIp) {
            int x = i & 0xFF;
            ip.append(x)
                    .append(".");
        }
//        for (int i = 0; i<remoteIp.length; i++){
//            int x = i & 0xFF;
//            ip.append(x);
//            if (i != remoteIp.length-1){
//                ip.append(".");
//            }
//        }
        return ip.toString();
    }

    public static byte[] fetchIp(String ipAdress) {
        String[] splitted = ipAdress.split("\\.");
        if (splitted.length != 4) {
            return null;
        }
        byte[] macBytes = new byte[4];
        for (int i = 0; i < splitted.length; i++) {
            try {
                int number = Integer.parseInt(splitted[i]);
                if (number > 255) {
                    return null;
                } else {
                    macBytes[i] = (byte) number;
                }
            } catch (NumberFormatException e){
                return null;
            }

        }
        return macBytes;
    }

    public static byte[] fetchReverseIp(String ipAdress) {
        String[] splitted = ipAdress.split("\\.");
        if (splitted.length != 4) {
            return null;
        }
        byte[] macBytes = new byte[4];
        for (int i = 0; i < splitted.length; i++) {
            try {
                int number = Integer.parseInt(splitted[i]);
                if (number > 255) {
                    return null;
                } else {
                    macBytes[3-i] = (byte) number;
                }
            } catch (NumberFormatException e){
                return null;
            }

        }
        return macBytes;
    }


    public static String getCommandName(byte lastCommand) {
        String command = "0x" + Integer.toHexString(lastCommand & 0xFF).toUpperCase();
        switch (command) {
            case Commands.CMD_AUTHENTICATION:
                return "Authentication";
            case Commands.CMD_CONNECTION_TIMEOUT:
                return "Set ConnectionTimeout";
            case Commands.CMD_ENABLE_IPV4_6:
                return "Enable IPV4/IPV6";
            case Commands.CMD_ERASE_POWER_OUTAGE:
                return "Erase Power Outage";
            case Commands.CMD_FIRMWARE_UPGRADE:
                return "Firmware Upgrade";
            case Commands.CMD_GET_CONTROLLER_IP:
                return "Get Controller Ip";
            case Commands.CMD_GET_FIRMWARE_VER:
                return "Get Firmware Version";
            case Commands.CMD_GET_INPUT_CONFIG:
                return "Get Input Config";
//            case Commands.CMD_GET_IP_VERSION:
//                return "Get IP Version";
            case Commands.CMD_GET_NTP_SERVER:
                return "Get NTP Server";
            case Commands.CMD_GET_OEM_ID:
                return "Get OEM Id";
            case Commands.CMD_GET_PORT_ID:
                return "Get Port Id";
            case Commands.CMD_GET_REMOTE_IP_DNS:
                return "Get Remote IP Dns";
            case Commands.CMD_GET_RTC:
                return "Get RTC";
            case Commands.CMD_LAST_UPDATE_DETAIL:
                return "Last Update Details";
            case Commands.CMD_REBOOT_TARGET:
                return "Reboot Target";
            case Commands.CMD_RESET_TARGET:
                return "Reset Target";
            case Commands.CMD_SET_INPUT_CONFIG:
                return "Set Input Config";
            case Commands.CMD_SET_NTP_SERVER:
                return "Set Ntp Server";
            case Commands.CMD_SET_PORT_ID:
                return "Set Port Id";
            case Commands.CMD_SET_REMOTE_IP_DNS:
                return "Set Remote Ip Dns";
            case Commands.CMD_SET_RTC:
                return "Set RTC";
            case Commands.CMD_UPDATE_IP:
                return "Update IP";
        }
        return "Unknown Command";
    }

    public static String getStatus(byte statusByte) {
        switch (statusByte) {
            case Constants.SUCCESS:
                return "Successfull";
            case Constants.FAILED:
                return "Failed";
            case Constants.INCORRECT_PARAMS:
                return "Incorrect Params";
            case Constants.FIRMWARE_DOWNLOAD_FAILED:
                return "Firmware Download Failed";
            case Constants.INCORRECT_PASSWORD:
                return "Incorrect Password";
            case Constants.UNKNOWN_COMMAND:
                return "Unknown Command";
        }
        return "Unknown Command";
    }
}
