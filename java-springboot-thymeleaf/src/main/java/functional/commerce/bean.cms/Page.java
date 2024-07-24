package functional.commerce.bean.cms;

import com.fasterxml.jackson.annotation.JsonInclude;
import functional.preview.PreviewComponent;
import hybrisclient.model.EntryMap;
import hybrisclient.model.HrefLangWsDTO;
import hybrisclient.model.custom.CmsComponent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Getter
@Setter
@ToString
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Page implements Serializable {

    private String catalogVersionUuid;
    private String uid;
    private String uuid;
    private String name;
    private String template = "template__1-1__fullwidth";
    private String typeCode;
    private boolean follow;
    private boolean index;
    private String grLoadingFirst;
    private String grOnetrustCategory;
    private String label;

    private PreviewComponent previewComponent;

    private List<HrefLangWsDTO> grHreflangs = new ArrayList<>();
    private Map<String, List<EntryMap>> localizedUrls = new LinkedHashMap<>();

    // custom
    private Meta meta = new Meta();
    private List<ContentSlot> contentSlots = new ArrayList<>();
    private List<BreadcrumbItem> breadcrumbs = new ArrayList<>();

    public Page(String uid, CmsComponent... components) {
        this.uid = uid;
        this.name = uid;

        final ContentSlot contentSlot = new ContentSlot();
        contentSlot.setPosition("Section1");
        contentSlot.setSlotId(contentSlot.getPosition());
        contentSlot.setComponents(Arrays.asList(components));

        this.getContentSlots().add(contentSlot);
    }

}
