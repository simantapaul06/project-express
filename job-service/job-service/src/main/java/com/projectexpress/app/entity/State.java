package com.projectexpress.app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cache;

@Entity
@Table(name = "state",
       uniqueConstraints = {
            @UniqueConstraint(
                name = "uk_state_country",
                columnNames = {"name", "country_id"}
            )
       }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class State {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;
}
