package com.projectexpress.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "cities",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_city_state",
                        columnNames = {"name", "state_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "state_id", nullable = false)
    private State state;
}