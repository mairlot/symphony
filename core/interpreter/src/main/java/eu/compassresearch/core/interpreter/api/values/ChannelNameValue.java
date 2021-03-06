package eu.compassresearch.core.interpreter.api.values;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.overture.ast.analysis.AnalysisException;
import org.overture.interpreter.values.Value;

import eu.compassresearch.core.interpreter.api.InterpreterRuntimeException;

public class ChannelNameValue extends Value
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2916244711082591555L;

	private final CMLChannelValue channel;
	private final List<Value> values;
	private final List<ValueConstraint> constraints;

	public ChannelNameValue(CMLChannelValue channel, List<Value> values,
			List<ValueConstraint> constraints)
	{
		this.channel = channel;
		this.values = values;
		this.constraints = constraints;
	}

	public ChannelNameValue(CMLChannelValue channel, List<Value> values)
	{
		this(channel, values, new LinkedList<ValueConstraint>());
		for (int i = 0; i < values.size(); i++)
		{
			this.constraints.add(new NoConstraint());
		}
	}

	public ChannelNameValue(CMLChannelValue channel)
	{
		this(channel, new LinkedList<Value>(), new LinkedList<ValueConstraint>());
	}

	/*
	 * Copy constructor
	 */
	protected ChannelNameValue(ChannelNameValue other)
	{
		this.channel = other.channel;
		this.values = new LinkedList<Value>(other.values);
		this.constraints = new LinkedList<ValueConstraint>(other.constraints);
	}

	@Override
	public String toString()
	{

		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(this.channel.getName());
		for (Value v : values)
		{
			strBuilder.append("." + v);
		}

		return strBuilder.toString();
	}

	@Override
	public boolean equals(Object other)
	{

		ChannelNameValue otherChannelName = null;

		if (!(other instanceof ChannelNameValue))
		{
			return false;
		}

		otherChannelName = (ChannelNameValue) other;

		return otherChannelName.getChannel().equals(this.getChannel())
				&& otherChannelName.getValues().equals(this.getValues());
	}

	@Override
	public int hashCode()
	{
		return channel.hashCode() + values.hashCode();
	}

	@Override
	public String kind()
	{
		return "ChannelName";
	}

	@Override
	public Object clone()
	{
		return new ChannelNameValue(channel, new LinkedList<Value>(values));
	}

	/**
	 * This represents the channel id e.g a.x.y.z would refer to the channel a
	 * 
	 * @return
	 */
	public CMLChannelValue getChannel()
	{
		return channel;
	}

	/**
	 * This represents the values after the channel id e.g a.x.y.z would have 3 values x, y and z
	 * 
	 * @return
	 */
	public List<Value> getValues()
	{
		return values;
	}

	public boolean hasValues()
	{
		return this.values.size() > 0;
	}

	public boolean isPrecise()
	{
		return AbstractValueInterpreter.isValueMostPrecise(this);
	}

	public ChannelNameValue meet(ChannelNameValue other)
	{
		List<Value> meetValues = new LinkedList<Value>();
		List<ValueConstraint> meetConstraints = new LinkedList<ValueConstraint>();
		// find the meet value and make a copy
		for (int i = 0; i < values.size(); i++)
		{
			meetValues.add(AbstractValueInterpreter.meet(this.values.get(i), other.values.get(i)));
		}

		// Combine all the constraint as a MultiConstraint
		for (int i = 0; i < constraints.size(); i++)
		{
			meetConstraints.add(MultiConstraint.combine(this.constraints.get(i), other.constraints.get(i)));
		}

		return new ChannelNameValue(this.channel, meetValues, meetConstraints);
	}

	
	public ChannelNameValue rename(CMLChannelValue channel)
	{
		return new ChannelNameValue(channel, values, constraints);
	}
	
	public boolean isComparable(ChannelNameValue channelNameValue)
	{
		return this.getChannel().equals(channelNameValue.channel)
				&& isValuesComparable(channelNameValue);
	}

	private boolean isValuesComparable(ChannelNameValue channelNameValue)
	{
		for (Iterator<Value> thisIt = values.listIterator(), otherIt = channelNameValue.values.listIterator(); thisIt.hasNext()
				&& otherIt.hasNext();)
		{
			Value thisValue = thisIt.next();
			Value otherValue = otherIt.next();

			// if both values are not loose and they differ then we cannot compare them
			if (AbstractValueInterpreter.isValueMostPrecise(thisValue)
					&& AbstractValueInterpreter.isValueMostPrecise(otherValue)
					&& !thisValue.equals(otherValue))
			{
				return false;
			}

		}

		return true;
	}

	public boolean isGTEQPrecise(ChannelNameValue other)
	{
		boolean res = true;
		for (int i = 0; i < values.size(); i++)
		{
			Value thisValue = values.get(i);
			Value otherValue = other.values.get(i);
			res &= AbstractValueInterpreter.isEquallyOrMorePrecise(otherValue, thisValue);
		}

		return res;
	}

	public boolean isConstraintValid() throws AnalysisException
	{
		for (int i = 0; i < values.size(); i++)
		{
			if (!constraints.get(i).isValid(values.get(i)))
			{
				return false;
			}
		}

		return true;
	}

	public void updateValues(List<Value> values)
	{
		for (int i = 0; i < values.size(); i++)
		{
			if (this.values.size() < i)
			{
				updateValue(i, values.get(i));
			} else
			{
				this.values.add(values.get(i));
			}
		}
	}

	public void updateValue(int index, Value value)
	{
		if (value == null)
		{
			throw new InterpreterRuntimeException("Null value parsed to channel name value update index: "
					+ index);
		}
		values.set(index, value);
	}
}
