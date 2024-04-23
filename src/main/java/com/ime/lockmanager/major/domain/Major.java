package com.ime.lockmanager.major.domain;

import com.ime.lockmanager.common.domain.BaseTimeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Entity(name = "MAJOR_TABLE")
public class Major extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    public static Major of(String name) {
        return Major.builder()
                .name(name)
                .build();
    }

    public String changeName(String modifiedRepresentName) {
        this.name = modifiedRepresentName;
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        Major major = (Major) obj;
        if (!this.id.equals(major.id)) {
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
