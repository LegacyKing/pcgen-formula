/*
 * Copyright 2016 (C) Tom Parker <thpr@users.sourceforge.net>
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package pcgen.base.formula.inst;

import org.junit.Test;

import junit.framework.TestCase;
import pcgen.base.formula.base.LegalScopeLibrary;
import pcgen.base.formula.base.ScopeInstance;
import pcgen.base.formula.base.VarScoped;

public class ScopeInstanceFactoryTest extends TestCase
{

	private ScopeInstanceFactory factory;
	private LegalScopeLibrary library;
	private SimpleLegalScope scope;
	private ScopeInstance scopeInst;
	private SimpleLegalScope local;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		library = new LegalScopeLibrary();
		factory = new ScopeInstanceFactory(library);
		scope = new SimpleLegalScope(null, "Global");
		library.registerScope(scope);
		scopeInst = factory.getGlobalInstance("Global");
		local = new SimpleLegalScope(scope, "Local");
		library.registerScope(local);
	}

	@Test
	public void testConstructor()
	{
		try
		{
			new ScopeInstanceFactory(null);
			fail("null library must be rejected");
		}
		catch (NullPointerException | IllegalArgumentException e)
		{
			//ok
		}
	}

	public void testGetGlobalInstance()
	{
		try
		{
			factory.getGlobalInstance("Local");
			fail("Expected failure due to non global scope");
		}
		catch (IllegalArgumentException e)
		{
			//ok
		}
		ScopeInstance globalInst = factory.getGlobalInstance("Global");
		assertNull(globalInst.getParentScope());
		assertEquals("Global", globalInst.getLegalScope().getName());
		assertEquals(scopeInst, globalInst);
	}

	public void testGet()
	{
		ScopeInstance gsi = factory.get("Global", null);
		assertNull(gsi.getParentScope());
		assertEquals("Global", gsi.getLegalScope().getName());

		/*
		 * This is subtle, but important.
		 * 
		 * The case above of "null" being the argument means that it is "truly"
		 * the global scope - it's global being requested as "known" global.
		 * 
		 * The case below represents a "local" object without a unique scope -
		 * so the only valid scope is the global scope. Therefore, it's
		 * requested with a VarScoped object, but we expect the returned value
		 * to still be the global scope. We can see that in the equals tests
		 * below of si and gsi.
		 */
		Scoped gvs = new Scoped("Var", null, null);
		ScopeInstance si = factory.get("Global", gvs);
		assertNull(si.getParentScope());
		assertEquals("Global", si.getLegalScope().getName());
		assertEquals(si, gsi);
		assertTrue(si == gsi);

		try
		{
			factory.get("Local", gvs);
			fail("Mixmatch Local and Global should fail");
		}
		catch (IllegalArgumentException e)
		{
			//ok
		}
		try
		{
			factory.get("Local", null);
			fail("Mixmatch Local and Global should fail");
		}
		catch (IllegalArgumentException e)
		{
			//ok
		}
		Scoped lvs = new Scoped("LVar", "Local", gvs);
		ScopeInstance lsi = factory.get("Local", lvs);
		assertTrue(local.equals(lsi.getLegalScope()));
		assertTrue(scopeInst.equals(lsi.getParentScope()));
		assertEquals("Local", lsi.getLegalScope().getName());

		SimpleLegalScope sublocal = new SimpleLegalScope(local, "SubLocal");
		library.registerScope(sublocal);
		Scoped slvs = new Scoped("SVar", "SubLocal", lvs);
		ScopeInstance slsi = factory.get("Local", slvs);
		assertTrue(local.equals(slsi.getLegalScope()));
		assertTrue(scopeInst.equals(slsi.getParentScope()));
		assertEquals("Local", slsi.getLegalScope().getName());

	}

	public final class Scoped implements VarScoped
	{

		private final String name;
		private final String scopeName;
		private final VarScoped parent;

		public Scoped(String s, String scopeName, VarScoped parent)
		{
			name = s;
			this.scopeName = scopeName;
			this.parent = parent;
		}

		@Override
		public String getKeyName()
		{
			return name;
		}

		@Override
		public String getLocalScopeName()
		{
			return scopeName;
		}

		@Override
		public VarScoped getVariableParent()
		{
			return parent;
		}

	}
}
