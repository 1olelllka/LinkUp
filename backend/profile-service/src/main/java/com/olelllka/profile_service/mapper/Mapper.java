package com.olelllka.profile_service.mapper;

public interface Mapper<E, D> {
    E toEntity(D d);
    D toDto(E e);
}
