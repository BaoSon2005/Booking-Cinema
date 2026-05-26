package com.example.bookingcinema.AdminScreen.Model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FoodOrder {
    private static final long DEFAULT_PICKUP_WINDOW_MS = 15L * 60L * 1000L;

    private String id;
    private String orderCode;
    private String customerName;
    private String status;
    private boolean urgent;
    private long createdAtMillis;
    private long deadlineMillis;
    private final List<String> items = new ArrayList<>();

    public FoodOrder() {
    }

    public static FoodOrder fromSnapshot(DocumentSnapshot doc) {
        FoodOrder order = new FoodOrder();
        order.id = doc.getId();
        order.orderCode = firstText(doc, "orderCode", "maDon", "code", "ticketCode");
        if (order.orderCode.isEmpty()) {
            String shortId = doc.getId().length() > 4 ? doc.getId().substring(doc.getId().length() - 4) : doc.getId();
            order.orderCode = "#F" + shortId.toUpperCase(Locale.ROOT);
        }
        if (!order.orderCode.startsWith("#")) {
            order.orderCode = "#" + order.orderCode;
        }

        order.customerName = firstText(doc, "customerName", "tenKhach", "name", "userName");
        order.status = firstText(doc, "status", "trangThai");
        if (order.status.isEmpty()) {
            order.status = "Đang chuẩn bị";
        }
        order.createdAtMillis = firstLong(doc, "createdAtMillis", "createdAt", "createdTime");
        order.deadlineMillis = firstLong(doc, "deadlineMillis", "pickupDeadlineMillis", "pickupTimeMillis", "receiveBeforeMillis");
        if (order.createdAtMillis == 0L) {
            order.createdAtMillis = readTimestampMillis(doc, "createdAt", "orderTime", "createdTime");
        }
        if (order.deadlineMillis == 0L) {
            long base = order.createdAtMillis == 0L ? System.currentTimeMillis() : order.createdAtMillis;
            order.deadlineMillis = base + DEFAULT_PICKUP_WINDOW_MS;
        }

        Boolean urgentFlag = firstBoolean(doc, "urgent", "isUrgent", "khancap", "khanCap");
        String priority = firstText(doc, "priority", "mucDo", "level");
        order.urgent = Boolean.TRUE.equals(urgentFlag)
                || priority.toLowerCase(Locale.ROOT).contains("urgent")
                || priority.toLowerCase(Locale.ROOT).contains("khẩn")
                || order.deadlineMillis - System.currentTimeMillis() <= 5L * 60L * 1000L;

        order.items.addAll(readItems(doc));
        if (order.items.isEmpty()) {
            order.items.add("1 Combo bắp nước cao cấp");
        }
        return order;
    }

    private static List<String> readItems(DocumentSnapshot doc) {
        List<String> result = new ArrayList<>();
        Object raw = firstValue(doc, "items", "foodItems", "foods", "monAn", "combos");
        if (raw instanceof List<?>) {
            for (Object item : (List<?>) raw) {
                String line = parseItemLine(item);
                if (!line.isEmpty()) {
                    result.add(line);
                }
            }
        } else if (raw instanceof Map<?, ?>) {
            String line = parseItemLine(raw);
            if (!line.isEmpty()) {
                result.add(line);
            }
        } else if (raw instanceof String) {
            String text = ((String) raw).trim();
            if (!text.isEmpty()) {
                result.add(text);
            }
        }

        String itemsText = firstText(doc, "itemsText", "moTaMon", "description");
        if (result.isEmpty() && !itemsText.isEmpty()) {
            result.add(itemsText);
        }
        return result;
    }

    private static String parseItemLine(Object raw) {
        if (raw instanceof String) {
            return ((String) raw).trim();
        }
        if (!(raw instanceof Map<?, ?>)) {
            return "";
        }
        Map<?, ?> map = (Map<?, ?>) raw;
        String name = stringFromMap(map, "name", "tenMon", "foodName", "title");
        if (name.isEmpty()) {
            name = "Món F&B";
        }
        long quantity = longFromMap(map, "quantity", "soLuong", "count");
        if (quantity <= 0L) {
            quantity = 1L;
        }
        return quantity + " " + name;
    }

    private static Object firstValue(DocumentSnapshot doc, String... keys) {
        for (String key : keys) {
            Object value = doc.get(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static String firstText(DocumentSnapshot doc, String... keys) {
        for (String key : keys) {
            String value = doc.getString(key);
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
            Object raw = doc.get(key);
            if (raw != null && !(raw instanceof List<?>) && !(raw instanceof Map<?, ?>)) {
                String text = String.valueOf(raw).trim();
                if (!text.isEmpty()) {
                    return text;
                }
            }
        }
        return "";
    }

    private static long firstLong(DocumentSnapshot doc, String... keys) {
        for (String key : keys) {
            Long value = doc.getLong(key);
            if (value != null) {
                return value;
            }
            Object raw = doc.get(key);
            if (raw instanceof Number) {
                return ((Number) raw).longValue();
            }
        }
        return 0L;
    }

    private static Boolean firstBoolean(DocumentSnapshot doc, String... keys) {
        for (String key : keys) {
            Boolean value = doc.getBoolean(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static long readTimestampMillis(DocumentSnapshot doc, String... keys) {
        for (String key : keys) {
            Object raw = doc.get(key);
            if (raw instanceof Timestamp) {
                return ((Timestamp) raw).toDate().getTime();
            }
        }
        return 0L;
    }

    private static String stringFromMap(Map<?, ?> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null && !String.valueOf(value).trim().isEmpty()) {
                return String.valueOf(value).trim();
            }
        }
        return "";
    }

    private static long longFromMap(Map<?, ?> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            if (value != null) {
                try {
                    return Long.parseLong(String.valueOf(value));
                } catch (NumberFormatException ignored) {
                    return 0L;
                }
            }
        }
        return 0L;
    }

    public String getId() {
        return id == null ? "" : id;
    }

    public String getOrderCode() {
        return orderCode == null ? "" : orderCode;
    }

    public String getCustomerName() {
        return customerName == null ? "" : customerName;
    }

    public String getStatus() {
        return status == null ? "Đang chuẩn bị" : status;
    }

    public boolean isUrgent() {
        return urgent;
    }

    public long getDeadlineMillis() {
        return deadlineMillis;
    }

    public boolean isDelivered() {
        String normalized = getStatus().toLowerCase(Locale.ROOT);
        return normalized.contains("đã giao")
                || normalized.contains("delivered")
                || normalized.contains("completed")
                || normalized.contains("hoàn thành");
    }

    public String getCountdownText() {
        long remainMs = getDeadlineMillis() - System.currentTimeMillis();
        if (remainMs <= 0L) {
            return "Quá hạn nhận món";
        }
        long minutes = Math.max(1L, remainMs / 60000L);
        return "Khách lấy trong: " + String.format(Locale.getDefault(), "%02d", minutes) + " Phút";
    }

    public String getItemsText() {
        StringBuilder builder = new StringBuilder();
        for (String item : items) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(item);
        }
        return builder.toString();
    }
}
