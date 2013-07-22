/*
 * This file is a component of thundr, a software library from 3wks.
 * Read more: http://www.3wks.com.au/thundr
 * Copyright (C) 2013 3wks, <thundr@3wks.com.au>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.threewks.thundr.jpa;

import com.google.common.collect.Maps;
import com.threewks.thundr.configuration.Environment;
import com.threewks.thundr.jpa.model.Beverage;
import com.threewks.thundr.jpa.rule.SetupPersistenceManager;
import com.threewks.thundr.jpa.rule.SetupTransaction;
import org.junit.*;
import org.junit.rules.ExpectedException;

import javax.persistence.EntityExistsException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class JpaTemplateIT {
	@ClassRule
	public static SetupPersistenceManager setupPersistenceManager = new SetupPersistenceManager("test");

	@Rule
	public SetupTransaction setupTransaction = SetupTransaction.rollback(setupPersistenceManager.getPersistenceManager());

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private JpaTemplate<Beverage> template;

	@Before
	public void before() {
		Environment.set("test");

		template = new JpaTemplate<Beverage>(setupPersistenceManager.getPersistenceManager(), Beverage.class);
	}

	@Test
	public void shouldReturnAccurateCount() {
		assertThat(template.count(), is(0l));

		addSampleData();

		assertThat(template.count(), is(2l));
	}

   	@Test
	public void shouldRetrieveEntityById() {
		addSampleData();

		Beverage beverage = new Beverage("Absinthe");
	 	template.persist(beverage);

		Beverage retrieved = template.get(beverage.id);
		assertThat(retrieved, is(notNullValue()));
		assertThat(retrieved, is(beverage));
	}

	@Test
	public void shouldReturnNullWhenAttemptingRetrieveNonExistentEntity() {
		addSampleData();

		Beverage beverage = template.get("yeah I don't exist...");
		assertThat(beverage, is(nullValue()));
	}

	@Test
	public void shouldReturnEntityReference() {
		Beverage beverage = new Beverage("Merlot");
		template.persist(beverage);

		Beverage fetched = template.getReference(beverage.id);
		assertThat(fetched, is(notNullValue()));
	}

	@Test
	public void shouldThrowExceptionWhenAttemptingRetrieveNonExistentEntityReference() {
		thrown.expect(Exception.class);

		addSampleData();

		template.getReference("made up");
	}

	@Test
	public void shouldContainEntity() {
		addSampleData();

		Beverage beverage = new Beverage("Passiona");
		template.persist(beverage);

		assertThat(template.contains(beverage), is(true));
	}

	@Test
	public void shouldNotContainEntity() {
		addSampleData();

		Beverage beverage = new Beverage("Water");
		assertThat(template.contains(beverage), is(false));
	}

	@Test
	public void shouldMergeEntity() {
		Beverage beverage1 = new Beverage("Milk");
		template.persist(beverage1);

		Beverage beverage2 = new Beverage("Chocolate milk");
		beverage2.id = beverage1.id;

		template.merge(beverage2);
		template.flush();

		template.refresh(beverage1);
		assertThat(beverage1.name, is(beverage2.name));
	}

	@Test
	public void shouldThrowExceptionWhenPersistingDuplicateEntity() {
		thrown.expect(EntityExistsException.class);

		Beverage beverage1 = new Beverage("White wine");
		template.persist(beverage1);

		Beverage beverage2 = new Beverage("Red wine");
		beverage2.id = beverage1.id;
		template.persist(beverage2);
	}

	@Test
	public void shouldRemoveEntity() {
		addSampleData();

		Beverage beverage = new Beverage("Saki");
		template.persist(beverage);
		assertThat(template.contains(beverage), is(true));

		template.remove(beverage);
		assertThat(template.contains(beverage), is(false));
	}

	@Test
	public void shouldNotComplainWhenRemovingUnknownEntity() {
		addSampleData();

		Beverage beverage = new Beverage("Orange juice");
		template.remove(beverage);
	}

	@Test
	public void shouldReturnAllEntitiesWhenQueryingWithUnboundedQuery() {
		addSampleData();

		List<Beverage> beverages = template.query("from Beverage");
		assertThat(beverages, is(notNullValue()));
		assertThat(beverages.size(), is(2));
	}

	@Test
	 public void shouldReturnEntityListWithOneElementWhenQueryingForExistingEntity() {
		addSampleData();

		List<Beverage> beverages = template.query("from Beverage where name = ?", "Coffee");
		assertThat(beverages, is(notNullValue()));
		assertThat(beverages.size(), is(1));
	}

	@Test
	public void shouldReturnEmptyListWhenQueryingForNonExistentEntity() {
		addSampleData();

		List<Beverage> beverages = template.query("from Beverage where name = ?", "Motor oil");
		assertThat(beverages, is(notNullValue()));
		assertThat(beverages.size(), is(0));
	}

	@Test
	public void shouldReturnEntityListWhenQueryingWithNamedParameter() {
		addSampleData();

		Map<String, Object> params = Maps.newHashMap();
		params.put("name", "Coffee");

		List<Beverage> beverages = template.query("from Beverage where name = :name", params);
		assertThat(beverages, is(notNullValue()));
		assertThat(beverages.size(), is(1));
	}

	@Test
	public void shouldReturnEntityListWhenUsingNamedQueryWithoutParameters() {
		addSampleData();

		List<Beverage> beverages = template.namedQuery("Beverage.findAllAlcoholicBeverages");
		assertThat(beverages, is(notNullValue()));
		assertThat(beverages.size(), is(greaterThanOrEqualTo(1)));
	}

	@Test
	public void shouldReturnEntityListWhenUsingNamedQueryWithParameters() {
		addSampleData();

		List<Beverage> beverages = template.namedQuery("Beverage.findAllByName", "Beer");
		assertThat(beverages, is(notNullValue()));
		assertThat(beverages.size(), is(greaterThanOrEqualTo(1)));
	}

	@Test
	public void shouldReturnEntityListWhenUsingNamedQueryWithParameterMap() {
		addSampleData();

		Map<String, Object> params = Maps.newHashMap();
		params.put("alcoholic", false);

		List<Beverage> beverages = template.namedQuery("Beverage.findAllByType", params);
		assertThat(beverages, is(notNullValue()));
		assertThat(beverages.size(), is(greaterThanOrEqualTo(1)));
	}

	private void addSampleData() {
		template.persist(new Beverage("Coffee", false));
		template.persist(new Beverage("Beer", true));
	}
}