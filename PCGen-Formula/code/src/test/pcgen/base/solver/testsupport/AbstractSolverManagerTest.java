/*
 * Copyright 2014 (C) Tom Parker <thpr@users.sourceforge.net>
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
package pcgen.base.solver.testsupport;

import org.junit.Test;

import pcgen.base.formula.base.LegalScope;
import pcgen.base.formula.base.ScopeInstance;
import pcgen.base.formula.base.VariableID;
import pcgen.base.formula.base.VariableLibrary;
import pcgen.base.formula.base.WriteableVariableStore;
import pcgen.base.formula.inst.ComplexNEPFormula;
import pcgen.base.formula.inst.SimpleLegalScope;
import pcgen.base.solver.Modifier;
import pcgen.base.solver.SolverFactory;
import pcgen.base.solver.SolverManager;
import pcgen.base.testsupport.AbstractFormulaTestCase;

public abstract class AbstractSolverManagerTest extends AbstractFormulaTestCase
{

	private SolverFactory solverFactory = new SolverFactory();
	private VariableLibrary varLibrary;
	private WriteableVariableStore store;
	private LegalScope globalScope;
	private ScopeInstance globalScopeInst;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		varLibrary = getVariableLibrary();
		store = getVariableStore();
		globalScope = getGlobalScope();
		globalScopeInst = getGlobalScopeInst();
		solverFactory.addSolverFormat(Number.class,
			AbstractModifier.setNumber(0, 0));
	}

	protected abstract SolverManager getManager();

	@Test
	public void testIllegalCreateChannel()
	{
		varLibrary.assertLegalVariableID("HP", globalScope, numberManager);
		try
		{
			getManager().createChannel(null);
			fail();
		}
		catch (IllegalArgumentException | NullPointerException e)
		{
			//ok
		}
	}

	@Test
	public void testCreateChannelTwice()
	{
		varLibrary.assertLegalVariableID("HP", globalScope, numberManager);
		VariableID<Number> hp =
				(VariableID<Number>) varLibrary.getVariableID(globalScopeInst,
					"HP");
		getManager().createChannel(hp);
		try
		{
			getManager().createChannel(hp);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			//ok
		}
	}

	@Test
	public void testCreateChannel()
	{
		varLibrary.assertLegalVariableID("HP", globalScope, numberManager);
		VariableID<Number> hp =
				(VariableID<Number>) varLibrary.getVariableID(globalScopeInst,
					"HP");
		assertEquals(null, store.get(hp));
		getManager().createChannel(hp);
		assertEquals(0, store.get(hp));
	}

	@Test
	public void testIllegalAddModifier()
	{
		varLibrary.assertLegalVariableID("HP", globalScope, numberManager);
		VariableID<Number> hp =
				(VariableID<Number>) varLibrary.getVariableID(globalScopeInst,
					"HP");
		getManager().createChannel(hp);
		AbstractModifier<Number> modifier = AbstractModifier.setNumber(6, 5);
		ScopeInstance source = globalScopeInst;
		try
		{
			getManager().addModifier(null, modifier, source);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			//ok
		}
		try
		{
			getManager().addModifier(hp, null, source);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			//ok
		}
		try
		{
			getManager().addModifier(hp, modifier, null);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			//ok
		}
		//Invalid ID very bad
		VariableLibrary altLibrary = new VariableLibrary(getScopeLibrary());
		altLibrary.assertLegalVariableID("brains", globalScope, numberManager);
		VariableID<Number> brains =
				(VariableID<Number>) altLibrary.getVariableID(globalScopeInst,
					"Brains");
		try
		{
			getManager().addModifier(brains, modifier, source);
			fail("Didn't own that VarID!");
		}
		catch (IllegalArgumentException e)
		{
			//ok
		}
	}

	@Test
	public void testAddModifier()
	{
		varLibrary.assertLegalVariableID("HP", globalScope, numberManager);
		VariableID<Number> hp =
				(VariableID<Number>) varLibrary.getVariableID(globalScopeInst,
					"HP");
		assertEquals(null, store.get(hp));
		getManager().createChannel(hp);
		assertEquals(0, store.get(hp));
		ScopeInstance source = globalScopeInst;
		AbstractModifier<Number> modifier = AbstractModifier.setNumber(6, 5);
		getManager().addModifier(hp, modifier, source);
		assertEquals(6, store.get(hp));

		//Create not required...
		varLibrary.assertLegalVariableID("HitPoints", globalScope, numberManager);
		VariableID<Number> hitpoints =
				(VariableID<Number>) varLibrary.getVariableID(globalScopeInst,
					"HitPoints");
		assertEquals(null, store.get(hitpoints));
		getManager().addModifier(hitpoints, modifier, source);
		assertEquals(6, store.get(hitpoints));

		SimpleLegalScope localScope = new SimpleLegalScope(globalScope, "STAT");
		getScopeLibrary().registerScope(localScope);
		ScopeInstance strInst = getInstanceFactory().get("STAT", new MockStat("Strength"));

		getManager().addModifier(hitpoints, AbstractModifier.setNumber(12, 3), strInst);
		assertEquals(6, store.get(hitpoints));
		getManager().removeModifier(hitpoints, modifier, source);
		assertEquals(12, store.get(hitpoints));
	}

	@Test
	public void testComplex()
	{
		ScopeInstance source = globalScopeInst;
		ComplexNEPFormula formula = new ComplexNEPFormula("arms+legs");
		Modifier<Number> formulaMod = AbstractModifier.add(formula, 100);
		varLibrary.assertLegalVariableID("Limbs", globalScope, numberManager);
		varLibrary.assertLegalVariableID("arms", globalScope, numberManager);
		varLibrary.assertLegalVariableID("legs", globalScope, numberManager);
		VariableID<Number> limbs =
				(VariableID<Number>) varLibrary.getVariableID(globalScopeInst,
					"Limbs");
		assertEquals(null, store.get(limbs));
		getManager().addModifier(limbs, formulaMod, source);
		assertEquals(0, store.get(limbs));

		AbstractModifier<Number> two = AbstractModifier.setNumber(2, 5);
		VariableID<Number> arms =
				(VariableID<Number>) varLibrary.getVariableID(globalScopeInst,
					"Arms");
		assertEquals(0, store.get(arms));
		getManager().addModifier(arms, two, source);
		assertEquals(2, store.get(arms));
		assertEquals(2, store.get(limbs));
		
		AbstractModifier<Number> four = AbstractModifier.setNumber(4, 5);
		VariableID<Number> legs =
				(VariableID<Number>) varLibrary.getVariableID(globalScopeInst,
					"Legs");
		assertEquals(0, store.get(legs));
		getManager().addModifier(legs, four, source);
		assertEquals(2, store.get(arms));
		assertEquals(4, store.get(legs));
		assertEquals(6, store.get(limbs));

		getManager().removeModifier(arms, two, source);
		assertEquals(0, store.get(arms));
		assertEquals(4, store.get(legs));
		assertEquals(4, store.get(limbs));
	}

	@Test
	public void testChained()
	{
		ScopeInstance source = globalScopeInst;
		ComplexNEPFormula formula = new ComplexNEPFormula("arms+legs");
		Modifier<Number> limbsMod = AbstractModifier.add(formula, 100);

		ComplexNEPFormula handsformula = new ComplexNEPFormula("fingers/5");
		Modifier<Number> handsMod = AbstractModifier.add(handsformula, 100);

		varLibrary.assertLegalVariableID("Limbs", globalScope, numberManager);
		VariableID<Number> limbs =
				(VariableID<Number>) varLibrary.getVariableID(globalScopeInst,
					"Limbs");
		varLibrary.assertLegalVariableID("arms", globalScope, numberManager);
		VariableID<Number> arms =
				(VariableID<Number>) varLibrary.getVariableID(globalScopeInst,
					"Arms");
		varLibrary.assertLegalVariableID("Fingers", globalScope, numberManager);
		VariableID<Number> fingers =
				(VariableID<Number>) varLibrary.getVariableID(globalScopeInst,
					"Fingers");
		varLibrary.assertLegalVariableID("legs", globalScope, numberManager);
		VariableID<Number> legs =
				(VariableID<Number>) varLibrary.getVariableID(globalScopeInst,
					"Legs");
		assertEquals(null, store.get(limbs));
		getManager().addModifier(limbs, limbsMod, source);


		assertEquals(0, store.get(arms));
		getManager().addModifier(arms, handsMod, source);
		assertEquals(0, store.get(arms));

		AbstractModifier<Number> ten = AbstractModifier.setNumber(10, 5);
		getManager().addModifier(fingers, ten, source);
		assertEquals(2, store.get(arms));
		assertEquals(2, store.get(limbs));
		
		AbstractModifier<Number> four = AbstractModifier.setNumber(2, 5);
		assertEquals(0, store.get(legs));
		getManager().addModifier(legs, four, source);
		assertEquals(2, store.get(arms));
		assertEquals(2, store.get(legs));
		assertEquals(4, store.get(limbs));

		getManager().removeModifier(arms, handsMod, source);
		assertEquals(0, store.get(arms));
		assertEquals(2, store.get(legs));
		assertEquals(2, store.get(limbs));

	}

	@Test
	public void testIllegalRemoveModifier()
	{
		varLibrary.assertLegalVariableID("HP", globalScope, numberManager);
		VariableID<Number> hp =
				(VariableID<Number>) varLibrary.getVariableID(globalScopeInst,
					"HP");
		getManager().createChannel(hp);
		AbstractModifier<Number> modifier = AbstractModifier.setNumber(6, 5);
		ScopeInstance source = globalScopeInst;
		try
		{
			getManager().removeModifier(null, modifier, source);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			//ok
		}
		try
		{
			getManager().removeModifier(hp, null, source);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			//ok
		}
		try
		{
			getManager().removeModifier(hp, modifier, null);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			//ok
		}
		//Not present is Harmless
		getManager().removeModifier(hp, modifier, source);
		//Invalid ID very bad
		VariableLibrary altLibrary = new VariableLibrary(getScopeLibrary());
		altLibrary.assertLegalVariableID("brains", globalScope, numberManager);
		VariableID<Number> brains =
				(VariableID<Number>) altLibrary.getVariableID(globalScopeInst,
					"Brains");
		try
		{
			getManager().removeModifier(brains, modifier, source);
			fail("Didn't own that VarID!");
		}
		catch (IllegalArgumentException e)
		{
			//ok
		}
	}


	@Test
	public void testCircular()
	{
		ScopeInstance source = globalScopeInst;
		ComplexNEPFormula formula = new ComplexNEPFormula("arms+legs");
		Modifier<Number> limbsMod = AbstractModifier.add(formula, 100);

		ComplexNEPFormula handsformula = new ComplexNEPFormula("fingers/5");
		Modifier<Number> handsMod = AbstractModifier.add(handsformula, 100);

		ComplexNEPFormula fingersformula = new ComplexNEPFormula("limbs*5");
		Modifier<Number> fingersMod = AbstractModifier.add(fingersformula, 100);

		varLibrary.assertLegalVariableID("Limbs", globalScope, numberManager);
		VariableID<Number> limbs =
				(VariableID<Number>) varLibrary.getVariableID(globalScopeInst,
					"Limbs");
		varLibrary.assertLegalVariableID("arms", globalScope, numberManager);
		VariableID<Number> arms =
				(VariableID<Number>) varLibrary.getVariableID(globalScopeInst,
					"Arms");
		varLibrary.assertLegalVariableID("Fingers", globalScope, numberManager);
		VariableID<Number> fingers =
				(VariableID<Number>) varLibrary.getVariableID(globalScopeInst,
					"Fingers");
		varLibrary.assertLegalVariableID("legs", globalScope, numberManager);
		VariableID<Number> legs =
				(VariableID<Number>) varLibrary.getVariableID(globalScopeInst,
					"Legs");
		assertEquals(null, store.get(limbs));
		getManager().addModifier(limbs, limbsMod, source);

		assertEquals(0, store.get(arms));
		getManager().addModifier(arms, handsMod, source);
		assertEquals(0, store.get(arms));

		AbstractModifier<Number> ten = AbstractModifier.setNumber(10, 5);
		getManager().addModifier(fingers, ten, source);
		assertEquals(2, store.get(arms));
		assertEquals(2, store.get(limbs));
		
		AbstractModifier<Number> four = AbstractModifier.setNumber(2, 5);
		assertEquals(0, store.get(legs));
		getManager().addModifier(legs, four, source);
		assertEquals(2, store.get(arms));
		assertEquals(2, store.get(legs));
		assertEquals(4, store.get(limbs));

		try
		{
			getManager().addModifier(fingers, fingersMod, source);
			fail("How?");
		}
		catch (IllegalStateException e)
		{
			//yes, need to barf on infinite loop
		}

	}

	public SolverFactory getSolverFactory()
	{
		return solverFactory;
	}
	
	public VariableLibrary getVarLibrary()
	{
		return varLibrary;
	}

}
