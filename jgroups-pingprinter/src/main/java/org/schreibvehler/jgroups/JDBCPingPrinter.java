package org.schreibvehler.jgroups;

import org.jgroups.Address;
import org.jgroups.protocols.PingData;
import org.jgroups.util.Util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;

public class JDBCPingPrinter {
    public static void main(String[] args) throws Exception {
        try (Connection connection = DriverManager
                .getConnection("jdbc:postgresql://localhost:5432/keycloakdb", "admin", "password")) {
            PreparedStatement ps = connection.prepareStatement("SELECT OWN_ADDR, UPDATED, CLUSTER_NAME, PING_DATA FROM JGROUPSPING WHERE CLUSTER_NAME = ? ORDER BY UPDATED");
            ps.setString(1, "ejb");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    byte[] bytes = rs.getBytes("PING_DATA");
                    try {
                        PingData data = Util.streamableFromByteBuffer(PingData::new, bytes);
                        System.out.println(String.format("own_addr=%s; updated=%s; ping_data=%s;", rs.getString("OWN_ADDR"), rs.getTimestamp("UPDATED"), toString(data)));
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    private static String toString(PingData data) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(String.format("sender=%s; ", data.getAddress()));
        builder.append(String.format("name=%s; ", data.getLogicalName()));
        builder.append(String.format("addr=%s; ", data.getPhysicalAddr()));
        if (data.isCoord()) {
            builder.append("coord; ");
        } else if (data.isServer()) {
            builder.append("server; ");
        }
        builder.append(String.format("mbrs=%s;", toString(data.mbrs())));
        builder.append("]");

        return builder.toString();
    }

    private static String toString(Collection<? extends Address> mbrs) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        if (mbrs != null) {
            for (Address a : mbrs) {
                builder.append(a);
                builder.append(", ");
            }
        }
        builder.append("]");
        return builder.toString();
    }
}
