package com.ekuater.labelchat.command.confide;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;

/**
 * Created by Administrator on 2015/4/10.
 */
public class ConfideBatchPraiseCommand  extends UserCommand{

    private static final String URL = CommandUrl.CONFIDE_BATCH_PRAISE;

    public ConfideBatchPraiseCommand(){
        super();
        setUrl(URL);
    }

    public ConfideBatchPraiseCommand(String session, String userId){
        super(session, userId);
        setUrl(URL);
    }

    public void putParamConfideIdArray(String confideIdArray){
        putParam(CommandFields.Confide.CONFIDE_ID_ARRAY, confideIdArray);
    }

}
