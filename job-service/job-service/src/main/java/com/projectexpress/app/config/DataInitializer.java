package com.projectexpress.app.config;

import com.projectexpress.app.entity.City;
import com.projectexpress.app.entity.Country;
import com.projectexpress.app.entity.State;
import com.projectexpress.app.repository.CityRepository;
import com.projectexpress.app.repository.CountryRepository;
import com.projectexpress.app.repository.StateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;
    private final CityRepository cityRepository;

    @Override
    public void run(String... args) {

        // 1. India
        Country india = createCountry("India", "IND");

        State karnataka = createState("Karnataka", india);
        createCity("Bangalore", karnataka);
        createCity("Mysore", karnataka);
        createCity("Mangalore", karnataka);

        State westBengal = createState("West Bengal", india);
        createCity("Kolkata", westBengal);
        createCity("Howrah", westBengal);
        createCity("Durgapur", westBengal);

        State maharashtra = createState("Maharashtra", india);
        createCity("Mumbai", maharashtra);
        createCity("Pune", maharashtra);
        createCity("Nagpur", maharashtra);

        // 2. United States
        Country usa = createCountry("United States", "USA");

        State california = createState("California", usa);
        createCity("Los Angeles", california);
        createCity("San Francisco", california);
        createCity("San Diego", california);

        State newYork = createState("New York", usa);
        createCity("New York City", newYork);
        createCity("Buffalo", newYork);
        createCity("Albany", newYork);

        // 3. Canada
        Country canada = createCountry("Canada", "CAN");

        State ontario = createState("Ontario", canada);
        createCity("Toronto", ontario);
        createCity("Ottawa", ontario);

        State bc = createState("British Columbia", canada);
        createCity("Vancouver", bc);
        createCity("Victoria", bc);

        // 4. United Kingdom
        Country uk = createCountry("United Kingdom", "GBR");

        State england = createState("England", uk);
        createCity("London", england);
        createCity("Manchester", england);

        State scotland = createState("Scotland", uk);
        createCity("Edinburgh", scotland);
        createCity("Glasgow", scotland);

        // 5. Australia
        Country australia = createCountry("Australia", "AUS");

        State nsw = createState("New South Wales", australia);
        createCity("Sydney", nsw);
        createCity("Newcastle", nsw);

        State victoria = createState("Victoria", australia);
        createCity("Melbourne", victoria);
        createCity("Geelong", victoria);

        // 6. Germany
        Country germany = createCountry("Germany", "DEU");

        State bavaria = createState("Bavaria", germany);
        createCity("Munich", bavaria);
        createCity("Nuremberg", bavaria);

        State berlinState = createState("Berlin", germany);
        createCity("Berlin", berlinState);

        // 7. Japan
        Country japan = createCountry("Japan", "JPN");

        State tokyoState = createState("Tokyo", japan);
        createCity("Tokyo", tokyoState);
        createCity("Yokohama", tokyoState);

        State osakaState = createState("Osaka", japan);
        createCity("Osaka", osakaState);
        createCity("Sakai", osakaState);

        // 8. Brazil
        Country brazil = createCountry("Brazil", "BRA");

        State saoPaulo = createState("Sao Paulo", brazil);
        createCity("Sao Paulo", saoPaulo);
        createCity("Campinas", saoPaulo);

        State rioDeJaneiro = createState("Rio de Janeiro", brazil);
        createCity("Rio de Janeiro", rioDeJaneiro);

        // 9. South Africa
        Country southAfrica = createCountry("South Africa", "ZAF");

        State gauteng = createState("Gauteng", southAfrica);
        createCity("Johannesburg", gauteng);
        createCity("Pretoria", gauteng);

        State westernCape = createState("Western Cape", southAfrica);
        createCity("Cape Town", westernCape);

        // 10. France
        Country france = createCountry("France", "FRA");

        State ileDeFrance = createState("Île-de-France", france);
        createCity("Paris", ileDeFrance);
        createCity("Versailles", ileDeFrance);

        State paca = createState("Provence-Alpes-Côte d'Azur", france);
        createCity("Marseille", paca);
        createCity("Nice", paca);
    }

    // --- Helper Methods to keep code clean and dry ---

    private Country createCountry(String name, String code) {
        return countryRepository.findByName(name)
                .orElseGet(() -> countryRepository.save(
                        Country.builder()
                                .name(name)
                                .code(code)
                                .build()
                ));
    }

    private State createState(String name, Country country) {
        return stateRepository.findByNameAndCountry(name, country)
                .orElseGet(() -> stateRepository.save(
                        State.builder()
                                .name(name)
                                .country(country)
                                .build()
                ));
    }

    private void createCity(String cityName, State state) {
        cityRepository.findByNameAndState(cityName, state)
                .orElseGet(() -> cityRepository.save(
                        City.builder()
                                .name(cityName)
                                .state(state)
                                .build()
                ));
    }
}