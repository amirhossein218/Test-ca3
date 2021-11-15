package org.springframework.samples.petclinic.model;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.samples.petclinic.model.priceCalculators.SimplePriceCalculator;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link SimplePriceCalculator}
 */
@RunWith(MockitoJUnitRunner.class)
public class SimplePriceCalculatorTest {
	private static final double BASE_CHARGE = 1000;
	private static final double BASE_PRICE_PER_PET = 25;
	private static final double DELTA = 0.001;
	private static final UserType USER_TYPE_NEW = UserType.NEW;
	private static final UserType USER_TYPE_SILVER = UserType.SILVER;
	private static final double BASE_RARE_COEF = 1.2;

	private static final SimplePriceCalculator simplePriceCalculator = new SimplePriceCalculator();

	private static final Pet pet_rare = mock(Pet.class);
	private static final Pet pet_common = mock(Pet.class);
	private static final PetType pet_type_rare = mock(PetType.class);
	private static final PetType pet_type_common = mock(PetType.class);

	private static List<Pet> pet_rare_list;
	private static List<Pet> pet_common_list;

	@BeforeClass
	public static void setup() {
		when(pet_type_common.getRare()).thenReturn(false);
		when(pet_type_rare.getRare()).thenReturn(true);
		when(pet_common.getType()).thenReturn(pet_type_common);
		when(pet_rare.getType()).thenReturn(pet_type_rare);
		pet_rare_list = Arrays.asList(pet_rare, pet_rare, pet_rare, pet_rare);
		pet_common_list = Arrays.asList(pet_common, pet_common, pet_common, pet_common, pet_common);
	}

	@Test
	public void calcPriceShouldReturnBaseChargeIfThereIsNoPet() {
		double result_price = simplePriceCalculator.calcPrice(Collections.emptyList(), BASE_CHARGE, BASE_PRICE_PER_PET, USER_TYPE_SILVER);
		assertEquals(BASE_CHARGE, result_price, DELTA);
	}

	@Test
	public void calcPriceShouldReturnDiscountedBaseChargeForUserTypeNewWhenPetListIsEmpty() {
		double result_price = simplePriceCalculator.calcPrice(Collections.emptyList(), BASE_CHARGE, BASE_PRICE_PER_PET, USER_TYPE_NEW);
		double target_price = BASE_CHARGE * USER_TYPE_NEW.discountRate;
		assertEquals(target_price, result_price, DELTA);
	}

	@Test
	public void calcPriceShouldUseAndApplyRareCoefForRarePets() {
		double result_price = simplePriceCalculator.calcPrice(pet_rare_list, BASE_CHARGE, BASE_PRICE_PER_PET, USER_TYPE_SILVER);
		double target_price = BASE_CHARGE + pet_rare_list.size() * BASE_PRICE_PER_PET * BASE_RARE_COEF;
		assertEquals(target_price, result_price, DELTA);
	}

	@Test
	public void calcPriceShouldNotUseRareCoefForCommonPets() {
		double result_price = simplePriceCalculator.calcPrice(pet_common_list, BASE_CHARGE, BASE_PRICE_PER_PET, USER_TYPE_SILVER);
		double target_price = BASE_CHARGE + pet_common_list.size() * BASE_PRICE_PER_PET;
		assertEquals(target_price, result_price, DELTA);
	}

	@Test
	public void calcPriceShouldApplyNewUserDiscountAfterAllOtherCalculations() {
		double result_price = simplePriceCalculator.calcPrice(Stream.concat(pet_rare_list.stream(), pet_common_list.stream()).collect(Collectors.toList()), BASE_CHARGE, BASE_PRICE_PER_PET, USER_TYPE_NEW);
		double target_price = (BASE_CHARGE + pet_common_list.size() * BASE_PRICE_PER_PET + pet_rare_list.size() * BASE_PRICE_PER_PET * BASE_RARE_COEF) * USER_TYPE_NEW.discountRate;
		assertEquals(target_price, result_price, DELTA);
	}
}
