package eu.compassresearch.core.typechecker;

import java.io.File;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class StatementsCmlTypeCheckerTests extends
		AbstractResultBasedCmlTypeCheckerTestCase
{
	public StatementsCmlTypeCheckerTests(File file, String name, TestType type)
	{
		super(file, name, type);
	}

	@Parameters(name = "{1}")
	public static Collection<Object[]> getData()
	{
		// return combine(collectResourcesTestData("statements",
		// TestType.POSITIVE),collectResourcesTestData("statements", TestType.NEGATIVE));
		return collectResourcesTestData("statements", TestType.POSITIVE, TestType.COMPARE_RECORDRD);
	}

	@Override
	protected String getPropertyId()
	{
		return "statements";
	}

}
