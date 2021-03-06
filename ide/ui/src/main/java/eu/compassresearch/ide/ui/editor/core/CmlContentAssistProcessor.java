package eu.compassresearch.ide.ui.editor.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Point;
import org.overture.ast.definitions.PDefinition;
import org.overture.ide.ui.editor.core.VdmDocument;

import eu.compassresearch.ide.core.resources.ICmlSourceUnit;

public class CmlContentAssistProcessor implements IContentAssistProcessor
{

	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int documentOffset)
	{
		// FIXME: this sometimes returns an VdmDocument
		VdmDocument doc = (VdmDocument) viewer.getDocument();
		ICmlSourceUnit csu = (ICmlSourceUnit) doc.getSourceUnit().getAdapter(ICmlSourceUnit.class);
		Point selectedRange = viewer.getSelectedRange();

		// Making sure of whats been asked
		System.out.println("Requested code completion in" + csu.toString()
				+ " at " + selectedRange.toString());

		List<ICompletionProposal> proposalList = new ArrayList<ICompletionProposal>();

		// if text was selected...
		if (selectedRange.y > 0)
		{
			try
			{
				String selectedText = doc.get(selectedRange.x, selectedRange.y);

				computeSelectedTextProposals(selectedText, selectedRange, proposalList);

			} catch (BadLocationException e)
			{
				// TODO add pretty error string
				e.printStackTrace();
			}
		} else
		{
			// find qualifier
			String qualifier = getQualifier(doc, documentOffset);
			computeQualifiedProposals(qualifier, documentOffset, csu, proposalList);
		}

		ICompletionProposal[] r = new ICompletionProposal[proposalList.size()];
		proposalList.toArray(r);
		return r;
	}

	private void computeSelectedTextProposals(String selectedText,
			Point selectedRange, List<ICompletionProposal> proposalList)
	{
		// TODO Auto-generated method stub

	}

	private void computeQualifiedProposals(String qualifier,
			int documentOffset, ICmlSourceUnit csu,
			List<ICompletionProposal> proposalList)
	{

		int qlen = qualifier.length();

		for (PDefinition spd : csu.getParseListDefinitions())
		{
			computeSingleProposal(spd, qualifier, documentOffset, qlen, proposalList);
			// if (spd.getName() != null)
			// if ((spd.getName().name).toLowerCase().startsWith(qualifier.toLowerCase())) {
			// int cursor = spd.getName().name.length();
			// CompletionProposal proposal = new CompletionProposal(
			// spd.getName().name, documentOffset-qlen,qlen,cursor);
			// proposalList.add(proposal);
			// }
		}
		// csu.getSourceAst().get
		System.out.println("Qualifier is " + qualifier);

	}

	// private void computeSingleProposal(ATypesDefinition definition,
	// String qualifier, int documentOffset, int qlen,
	// List<ICompletionProposal> proposalList)
	// {
	// for (ATypeDefinition atd : definition.getTypes())
	// {
	// computeSingleProposal(atd, qualifier, documentOffset, qlen, proposalList);
	// }
	// }

	private void computeSingleProposal(PDefinition pd, String qualifier,
			int documentOffset, int qlen, List<ICompletionProposal> proposalList)
	{
		if (pd.getName() != null)
			if ((pd.getName().getName()).toLowerCase().startsWith(qualifier.toLowerCase()))
			{
				int cursor = pd.getName().getName().length();
				CompletionProposal proposal = new CompletionProposal(pd.getName().getName(), documentOffset
						- qlen, qlen, cursor);
				proposalList.add(proposal);
			}

	}

	// private void computeSingleProposal(PDefinition definition,
	// String qualifier, int documentOffset, int qlen, List<ICompletionProposal> proposalList) {
	// if (definition.getName() != null)
	// if ((definition.getName().name).toLowerCase().startsWith(qualifier.toLowerCase())) {
	// int cursor = definition.getName().name.length();
	// CompletionProposal proposal = new CompletionProposal(
	// definition.getName().name, documentOffset-qlen,qlen,cursor);
	// proposalList.add(proposal);
	// }
	// }

	// TODO refactor this method. It's a mess
	private String getQualifier(VdmDocument doc, int documentOffset)
	{
		StringBuffer buff = new StringBuffer();
		try
		{
			if (Character.isWhitespace(doc.getChar(documentOffset))
					&& Character.isWhitespace(doc.getChar(documentOffset - 1)))
				return "";
			while (true)
			{
				// Read backwards
				char c = doc.getChar(--documentOffset);
				if (Character.isWhitespace(c))
					return buff.reverse().toString();
				buff.append(c);
			}

		} catch (BadLocationException e)
		{
			// at start, found nothing. quit
			return "";
		}
	}

	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int documentOffset)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public char[] getCompletionProposalAutoActivationCharacters()
	{
		return new char[] { '.' };
	}

	public char[] getContextInformationAutoActivationCharacters()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public IContextInformationValidator getContextInformationValidator()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getErrorMessage()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
