package io.shnflrsc.yare.repository;

import io.shnflrsc.yare.model.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File,Long> {
}
