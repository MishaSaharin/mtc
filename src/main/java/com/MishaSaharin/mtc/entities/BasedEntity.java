package com.MishaSaharin.mtc.entities;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.util.UUID;

@MappedSuperclass
public abstract class BasedEntity implements Persistable<UUID> {
    @Id
    @GeneratedValue
    private UUID uuid;

    public BasedEntity() {
    }

    public BasedEntity(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    private void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    @Transient
    public boolean isNew() {
        return uuid == null;
    }

    @Override
    public int hashCode() {
        return uuid == null ? 0 : uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + uuid;
    }

}