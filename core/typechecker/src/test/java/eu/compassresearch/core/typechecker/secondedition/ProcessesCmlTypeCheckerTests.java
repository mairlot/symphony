package eu.compassresearch.core.typechecker.secondedition;

import java.io.File;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class ProcessesCmlTypeCheckerTests extends
		AbstractResultBasedCmlTypeCheckerTestCase
{
	public ProcessesCmlTypeCheckerTests(File file, String name, TestType type)
	{
		super(file, name, type);
	}

	@Parameters(name = "{1}")
	public static Collection<Object[]> getData()
	{
		return combine(collectResourcesTestData("processes", TestType.POSITIVE), collectResourcesTestData("processes", TestType.NEGATIVE));
	}

	// @Before
	// public void setup()
	// {
	// Properties.recordTestResults = true;
	// }

}
