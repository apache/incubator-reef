//<auto-generated />

using System.Runtime.Serialization;

namespace Org.Apache.REEF.Common.Avro
{
    /// <summary>
    /// Used to serialize and deserialize Avro record org.apache.reef.webserver.AvroReefServiceInfo.
    /// </summary>
    [DataContract(Namespace = "org.apache.reef.webserver")]
    public partial class AvroReefServiceInfo
    {
        private const string JsonSchema = @"{""type"":""record"",""name"":""org.apache.reef.webserver.AvroReefServiceInfo"",""fields"":[{""name"":""serviceName"",""type"":""string""},{""name"":""serviceInfo"",""type"":""string""}]}";

        /// <summary>
        /// Gets the schema.
        /// </summary>
        public static string Schema
        {
            get
            {
                return JsonSchema;
            }
        }
      
        /// <summary>
        /// Gets or sets the serviceName field.
        /// </summary>
        [DataMember]
        public string serviceName { get; set; }
              
        /// <summary>
        /// Gets or sets the serviceInfo field.
        /// </summary>
        [DataMember]
        public string serviceInfo { get; set; }
                
        /// <summary>
        /// Initializes a new instance of the <see cref="AvroReefServiceInfo"/> class.
        /// </summary>
        public AvroReefServiceInfo()
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="AvroReefServiceInfo"/> class.
        /// </summary>
        /// <param name="serviceName">The serviceName.</param>
        /// <param name="serviceInfo">The serviceInfo.</param>
        public AvroReefServiceInfo(string serviceName, string serviceInfo)
        {
            this.serviceName = serviceName;
            this.serviceInfo = serviceInfo;
        }
    }
}