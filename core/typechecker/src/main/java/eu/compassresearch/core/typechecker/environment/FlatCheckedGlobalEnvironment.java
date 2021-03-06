package eu.compassresearch.core.typechecker.environment;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.overture.ast.definitions.PDefinition;
import org.overture.ast.intf.lex.ILexNameToken;
import org.overture.ast.node.Node;
import org.overture.ast.typechecker.NameScope;
import org.overture.typechecker.assistant.ITypeCheckerAssistantFactory;

import eu.compassresearch.core.typechecker.api.ICmlTypeChecker;

public class FlatCheckedGlobalEnvironment extends FlatStrictCheckEnvironment
{

	public FlatCheckedGlobalEnvironment(ITypeCheckerAssistantFactory af,
			List<PDefinition> definitions, NameScope scope)
	{
		super(af, definitions, scope);
	}

	@Override
	public PDefinition findType(ILexNameToken name, String fromModule)
	{
		PDefinition def = super.findType(name, fromModule);

		if (def == null)
		{
			def = super.findType(cloneToGlobal(name), fromModule);
		}

		return def;
	}

	public PDefinition findName(ILexNameToken name, NameScope scope)
	{
		PDefinition def = super.findName(name, scope);

		if (def == null)
		{
			def = super.findName(cloneToGlobal(name), scope);
		}

		return def;
	};

	public Set<PDefinition> findMatches(ILexNameToken name)
	{
		Set<PDefinition> defs = super.findMatches(name);

		if (defs == null || defs.isEmpty())
		{
			defs = super.findMatches(cloneToGlobal(name));
		}

		return defs;
	};

	/**
	 * Rewrites the name as if the module was empties in the original
	 * 
	 * @param name
	 * @return
	 */
	ILexNameToken cloneToGlobal(ILexNameToken name)
	{
		ILexNameToken globalName = name.clone();

		try
		{
			for (Field field : Node.getAllFields(new LinkedList<Field>(), globalName.getClass()))
			{
				if (field.getName().equals("module"))
				{
					field.setAccessible(true);
					field.set(globalName, ICmlTypeChecker.GLOBAL_CLASS_NAME);
					break;
				}
			}

		} catch (SecurityException | IllegalArgumentException
				| IllegalAccessException e)
		{

		}

		return globalName;
	}

	@Override
	public boolean isVDMPP()
	{
		return true;
	}
}
