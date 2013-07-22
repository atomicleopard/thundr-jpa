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

import org.junit.Test;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EntityManagerFactoryListenerTest {

	@Test
	public void shouldClearPersistenceManagerRegistryOnContextDestroyed() {
		PersistenceManagerRegistry registry = mock(PersistenceManagerRegistry.class);

		ServletContext context = mock(ServletContext.class);
		when(context.getAttribute(JpaInjectionConfiguration.PersistenceManagerRegistry)).thenReturn(registry);

		ServletContextEvent event = mock(ServletContextEvent.class);
		when(event.getServletContext()).thenReturn(context);

		EntityManagerFactoryListener listener = new EntityManagerFactoryListener();
		listener.contextDestroyed(event);

		verify(registry).clear();
	}
}