package technical.properties;

@Component
@ConfigurationProperties("end-points")
@Validated
@Getter
@Setter
@ToString
public class EndPointsProperties {

    private String gtmOptions;
    private String gtmOptionsWithoutOneTrust;
}
