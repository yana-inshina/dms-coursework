package ru.fa.dpi23.dmsisms.entity;

public enum ApplicationStatus {
    NEW,                // новая заявка
    APPROVED,           // одобрена менеджером
    REJECTED,           // отклонена
    CONVERTED_TO_POLICY // по заявке оформлен полис
}
