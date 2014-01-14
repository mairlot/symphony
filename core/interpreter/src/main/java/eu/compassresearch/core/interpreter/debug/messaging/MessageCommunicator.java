package eu.compassresearch.core.interpreter.debug.messaging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.overture.ast.intf.lex.ILexLocation;
import org.overture.ast.node.INode;
import org.overture.ast.node.Node;
import org.overture.ast.node.NodeList;
import org.overture.ast.types.PType;
import org.overture.interpreter.values.NaturalOneValue;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.KeyDeserializers;
import com.fasterxml.jackson.databind.deser.std.StdKeyDeserializer;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;

import eu.compassresearch.ast.lex.CmlLexNameToken;
import eu.compassresearch.ast.types.AChannelType;
import eu.compassresearch.core.interpreter.debug.Breakpoint;
import eu.compassresearch.core.interpreter.debug.CmlProcessDTO;

public class MessageCommunicator
{
	public static class DefaultCmlLexNameToken extends CmlLexNameToken
	{

		public DefaultCmlLexNameToken()
		{
			super(null, null, null, false, false);
		}
	}

	// public static class NodeListJsonWrapper extends NodeList
	// {
	//
	// public NodeListJsonWrapper()
	// {
	// super(null);
	// }
	//
	// }

	private static class ILexNameTokenDeserializer extends KeyDeserializer
	{

		@Override
		public Object deserializeKey(String key, DeserializationContext ctxt)
				throws IOException, JsonProcessingException
		{
			System.out.println("Strange key: " + key);
			return new DefaultCmlLexNameToken();
		}
	}

	private static ObjectMapper mapper = null;

	protected static ObjectMapper mapperInstance()
	{
		if (mapper == null)
		{
			abstract class MixIn
			{
				MixIn(@JsonProperty("module") String module,
						@JsonProperty("name") String name,
						@JsonProperty("location") ILexLocation location)
				{
				}

				@JsonIgnore
				List<PType> typeQualifier;
			}

			abstract class BreakpointMixIn
			{
				BreakpointMixIn(@JsonProperty("id") int id,
						@JsonProperty("file") String file,
						@JsonProperty("line") int line)
				{
				}

			}
			
			abstract class NaturalOneValueMixIn
			{
				NaturalOneValueMixIn(@JsonProperty("longVal") long value){}
				@JsonIgnore
				public  double value;
			}

			abstract class NodeListMixIn
			{
				NodeListMixIn( INode parent)
				{
				}
				
			}
			
			abstract class ChannelTypeMixIn
			{
//				@JsonIgnore//@JsonDeserialize(using=MyStaticEnumDeserializer.class)
//				private NodeList<PType> _parameters;
				
			}

			@JsonDeserialize(as = DefaultCmlLexNameToken.class, contentAs = DefaultCmlLexNameToken.class, keyAs = DefaultCmlLexNameToken.class)
			abstract class ILexNameTokenMixIn
			{

			}

			abstract class NodeMixIn
			{
				@JsonIgnore
				INode parent;
				@JsonIgnore
				Set _visitedNodes;
			}

			@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
			abstract class CmlProcessDTOMixIn
			{
			}

			class MyModule extends SimpleModule
			{
				public MyModule()
				{
					super("MyModule");
				}

				@Override
				public void setupModule(SetupContext context)
				{
					context.setMixInAnnotations(CmlLexNameToken.class, MixIn.class);
					context.setMixInAnnotations(org.overture.ast.lex.LexNameToken.class, MixIn.class);
					context.setMixInAnnotations(NaturalOneValue.class, NaturalOneValueMixIn.class);

//					context.setMixInAnnotations(org.overture.ast.intf.lex.ILexNameToken.class, ILexNameTokenMixIn.class);
					context.setMixInAnnotations(Node.class, NodeMixIn.class);
					context.setMixInAnnotations(Breakpoint.class, BreakpointMixIn.class);
					context.setMixInAnnotations(CmlProcessDTO.class, CmlProcessDTOMixIn.class);
					// and other set up, if any

					context.setMixInAnnotations(AChannelType.class, ChannelTypeMixIn.class);
					context.setMixInAnnotations(org.overture.ast.node.NodeList.class, NodeListMixIn.class);

					// this.addDeserializer(org.overture.ast.intf.lex.ILexNameToken.class, new StringKD());
					context.addKeyDeserializers(new KeyDeserializers()
					{

						@Override
						public KeyDeserializer findKeyDeserializer(
								JavaType type, DeserializationConfig config,
								BeanDescription beanDesc)
								throws JsonMappingException
						{
							if (type.getRawClass().equals(org.overture.ast.intf.lex.ILexNameToken.class))
							{
								return new ILexNameTokenDeserializer();
							}
							return null;
						}
					});

					context.appendAnnotationIntrospector(new AnnotationIntrospector()
					{

						@Override
						public Version version()
						{
							return VersionUtil.parseVersion("0.0.1", this.getClass().getPackage().getName(), "ignoredefinitions");
						}

						@Override
						public String[] findPropertiesToIgnore(Annotated ac)
						{
							// System.out.println(ac.getRawType().getName());
							if (PType.class.isAssignableFrom(ac.getRawType()))
							{
								return new String[] { "_definitions" };
							}
							return null;
						}

					});
				}

			}

			mapper = new ObjectMapper();
			MyModule module = new MyModule();
			// module.addAbstractTypeMapping(org.overture.ast.intf.lex.ILexNameToken.class,DefaultCmlLexNameToken.class);
			// module.a.addAbstractTypeMapping(org.overture.ast.node.NodeList.class, NodeListJsonWrapper.class);

			mapper.enableDefaultTyping();
			mapper.registerModule(module);
			mapper.enableDefaultTyping();
			mapper.configure(MapperFeature.AUTO_DETECT_GETTERS, false);
			mapper.configure(MapperFeature.AUTO_DETECT_IS_GETTERS, false);
			mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			mapper.configure(Feature.FLUSH_PASSED_TO_STREAM, false);
			mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
			mapper.configure(Feature.AUTO_CLOSE_TARGET, false);

		}

		return mapper;
	}

	public static void sendMessage(OutputStream outStream, Message message)
			throws JsonGenerationException, JsonMappingException, IOException
	{
		System.out.println("Sending..." + message);
		MessageContainer messageContainer = new MessageContainer(message);
		mapperInstance().writeValue(outStream, messageContainer);
		outStream.write(System.lineSeparator().getBytes());
		outStream.flush();
		System.out.println("Sendt..." + message);
		// PrintWriter writer = new PrintWriter(outStream);
		// writer.println(mapperInstance().toJson(messageContainer));
		// writer.flush();
	}

	/**
	 * Constructs a message by deserializing the output from requestReader and returns it in a CmlMessageContainer.
	 * 
	 * @param requestReader
	 * @return
	 * @throws IOException
	 */
	public static MessageContainer receiveMessage(BufferedReader requestReader)
			throws IOException
	{
		MessageContainer message = null;
		String strMessage = requestReader.readLine();
//		System.out.println("Read RAW:\n\t" + strMessage);
		if (strMessage != null)
		{
			message = mapperInstance().readValue(strMessage, MessageContainer.class);
		}
		// else
		// {
		// message = new MessageContainer(new CmlDbgStatusMessage(CmlDbgpStatus.CONNECTION_CLOSED));
		// }

		return message;
	}

	/**
	 * Constructs a message by deserializing the output from requestReader and returns it in a MessageContainer.
	 * 
	 * @param requestReader
	 * @return
	 * @throws IOException
	 */
	public static MessageContainer receiveMessage(BufferedReader requestReader,
			MessageContainer connectionClosedMessage) throws IOException
	{
		MessageContainer message = null;
		String strMessage = requestReader.readLine();
//		System.out.println("Read RAW:\n\t" + strMessage);
		if (strMessage != null)
		{
			message = mapperInstance().readValue(strMessage, MessageContainer.class);
		} else
		{
			message = connectionClosedMessage;
		}

		return message;
	}

}
