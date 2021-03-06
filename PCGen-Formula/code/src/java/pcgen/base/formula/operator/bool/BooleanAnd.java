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
package pcgen.base.formula.operator.bool;

import pcgen.base.formatmanager.FormatUtilities;
import pcgen.base.formula.base.OperatorAction;
import pcgen.base.formula.parse.Operator;
import pcgen.base.util.FormatManager;

/**
 * BooleanAnd performs the AND operation on two Boolean values.
 */
public class BooleanAnd implements OperatorAction
{

	/**
	 * Indicates that BooleanAnd Performs a logical AND.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public Operator getOperator()
	{
		return Operator.AND;
	}

	/**
	 * Performs Abstract Evaluation, checking that the two arguments are
	 * Boolean.class and returns BooleanManager.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public FormatManager<?> abstractEvaluate(Class<?> format1, Class<?> format2)
	{
		if (FormatUtilities.BOOLEAN_CLASS.isAssignableFrom(format1)
			&& FormatUtilities.BOOLEAN_CLASS.isAssignableFrom(format2))
		{
			return FormatUtilities.BOOLEAN_MANAGER;
		}
		return null;
	}

	/**
	 * Performs a logical AND on the given arguments.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public Object evaluate(Object left, Object right)
	{
		/*
		 * DO NOT inline these. This is intentional in order to catch Object l
		 * or Object r not being boolean...
		 */
		boolean leftBoolean = ((Boolean) left).booleanValue();
		boolean rightBoolean = ((Boolean) right).booleanValue();
		return Boolean.valueOf(leftBoolean && rightBoolean);
	}

}
