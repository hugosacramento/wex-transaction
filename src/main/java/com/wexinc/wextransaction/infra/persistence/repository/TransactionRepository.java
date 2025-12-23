package com.wexinc.wextransaction.infra.persistence.repository;

import com.wexinc.wextransaction.infra.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
}